package com.kotcrab.vis.ui.contrib.widget.file;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vne.win.thumbnails.WinThumbnailProvider;

/**
 * Fast and memory efficient thumbnail provider for Windows. Minimal supporting OS is Windows Vista. Before creating
 * call {@link #isPlatformSupported()} to check if current OS can support this provider.
 * <p>
 * Reburies to include following VNE libraries: vne-runtime and vne-win-thumbnails. See https://github.com/kotcrab/vne
 * @author Kotcrab
 */
public class WindowsFileChooserIconProvider extends CachingFileChooserIconProvider {
	private WinThumbnailProvider provider;

	public WindowsFileChooserIconProvider (FileChooser chooser) {
		super(chooser);
		provider = new WinThumbnailProvider();
	}

	@Override
	protected void scheduleThumbnailGeneration (final Thumbnail thumbnail, final FileChooser.ViewMode viewMode, final float thumbSize, final FileChooser.FileItem item) {
		executor.execute(new Runnable() {
			@Override
			public void run () {
				final int[] data = provider.getThumbnail(item.getFile().path().replace("/", "\\"), (int) chooser.getViewMode().getGridSize(chooser.getSizes()));
				if (data == null) return;
				updateImageFromData(data, thumbnail, viewMode, item);
			}
		});
	}

	private void updateImageFromData (final int[] data, final Thumbnail thumbnail, final FileChooser.ViewMode viewMode, final FileChooser.FileItem item) {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run () {
				Pixmap pixmap = new Pixmap(data[0], data[1], Pixmap.Format.RGBA8888);

				int row = 0;
				int column = 0;
				for (int i = 2; i < data.length; i++) {
					pixmap.drawPixel(row++, pixmap.getHeight() - column, data[i]);

					if (row >= pixmap.getWidth()) {
						row = 0;
						column++;
					}
				}

				Texture texture;
				texture = new Texture(pixmap);
				pixmap.dispose();

				thumbnail.addThumb(viewMode, texture);
				item.setIcon(thumbnail.getThumbnail(viewMode), Scaling.fit);
			}
		});
	}

	public static boolean isPlatformSupported () {
		return WinThumbnailProvider.isPlatformSupported();
	}

	@Override
	public void dispose () {
		super.dispose();
		provider.dispose();
	}
}
