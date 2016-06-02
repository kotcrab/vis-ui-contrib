package com.kotcrab.vis.ui.contrib.widget.file;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.widget.file.FileChooser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base implementation for icon providers. This is considered as internal API and should not be used directly.
 * @author Kotcrab
 */
public abstract class CachingFileChooserIconProvider extends HighResFileChooserIconProvider {
	private static final int MAX_CACHED = 600;
	private static final int MAX_THREADS = 1;

	private Array<Thumbnail> thumbnails = new Array<Thumbnail>();

	protected ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

	public CachingFileChooserIconProvider (FileChooser chooser) {
		super(chooser);
	}

	@Override
	protected Drawable getImageIcon (final FileChooser.FileItem item) {
		if (chooser.getViewMode().isThumbnailMode()) {
			final FileChooser.ViewMode viewMode = chooser.getViewMode();
			final float thumbSize = viewMode.getGridSize(chooser.getSizes());
			final FileHandle file = item.getFile();

			Thumbnail thumbnail = getThumbnail(file);
			if (thumbnail == null) {
				thumbnail = new Thumbnail(file);
				thumbnails.add(thumbnail);
			}

			if (thumbnail.getThumbnail(viewMode) != null) return thumbnail.getThumbnail(viewMode);

			scheduleThumbnailGeneration(thumbnail, viewMode, thumbSize, item);
		}

		return super.getImageIcon(item);
	}

	protected abstract void scheduleThumbnailGeneration (Thumbnail thumbnail, FileChooser.ViewMode viewMode, float thumbSize, FileChooser.FileItem item);

	private Thumbnail getThumbnail (FileHandle file) {
		for (Thumbnail thumbnail : thumbnails) {
			if (thumbnail.file.equals(file)) {
				return thumbnail;
			}
		}

		return null;
	}

	@Override
	public void directoryChanged (FileHandle newDirectory) {
		super.directoryChanged(newDirectory);
		restartThumbnailGeneration();
	}

	@Override
	public void viewModeChanged (FileChooser.ViewMode newViewMode) {
		super.viewModeChanged(newViewMode);
		restartThumbnailGeneration();
	}

	private void restartThumbnailGeneration () {
		executor.shutdownNow();
		executor = Executors.newFixedThreadPool(MAX_THREADS);
		optimizeCache();
	}

	@Override
	public void dispose () {
		super.dispose();
		executor.shutdownNow();
		for (Thumbnail thumbnail : thumbnails) {
			thumbnail.dispose();
		}
		thumbnails.clear();
	}

	private void optimizeCache () {
		if (thumbnails.size > MAX_CACHED) {
			for (int i = 0; i <= thumbnails.size - MAX_CACHED; i++) {
				thumbnails.get(i).dispose();
			}
			thumbnails.removeRange(0, thumbnails.size - MAX_CACHED);
		}
	}

	protected static class Thumbnail implements Disposable {
		private FileHandle file;
		private Texture textures[] = new Texture[3];
		private TextureRegionDrawable regions[] = new TextureRegionDrawable[3];

		public Thumbnail (FileHandle file) {
			this.file = file;
		}

		public void addThumb (FileChooser.ViewMode viewMode, Texture texture) {
			int index = -1;
			if (viewMode == FileChooser.ViewMode.SMALL_ICONS)
				index = 0;
			if (viewMode == FileChooser.ViewMode.MEDIUM_ICONS)
				index = 1;
			if (viewMode == FileChooser.ViewMode.BIG_ICONS)
				index = 2;

			textures[index] = texture;
			regions[index] = new TextureRegionDrawable(new TextureRegion(texture));
		}

		public Drawable getThumbnail (FileChooser.ViewMode viewMode) {
			if (viewMode == FileChooser.ViewMode.SMALL_ICONS)
				return regions[0];
			if (viewMode == FileChooser.ViewMode.MEDIUM_ICONS)
				return regions[1];
			if (viewMode == FileChooser.ViewMode.BIG_ICONS)
				return regions[2];
			return null;
		}

		@Override
		public void dispose () {
			if (textures[0] != null)
				textures[0].dispose();
			if (textures[1] != null)
				textures[1].dispose();
			if (textures[2] != null)
				textures[2].dispose();
		}
	}
}
