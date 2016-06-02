/*
 * Copyright 2014-2016 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kotcrab.vis.ui.contrib.widget.file;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.StreamUtils;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooser.FileIconProvider;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link FileIconProvider} implementation supporting displaying real files thumbnails. Imgscalr is used to create
 * thumbnails. Thumbnail generation is asynchronous. Before thumbnail is ready {@link HighResFileChooserIconProvider} is
 * used to provide temporary image.
 * <p>
 * Note about memory usage: this may cause heap to grow very quickly even though the actual memory used after thumbnail
 * generation is low. You can use JVM argument `-XX:MaxHeapFreeRatio=70` to make JVM release allocated heap memory quicker.
 * <p>
 * Warning: Test showed that most JVM can't handle CMYK JPGs correctly, such JPGs will have wrong colors in thumbnail preview.
 * For proper support for such JPGs different ImageIO JPG reader must be used for example CMYKJPEGImageReader from Monte
 * Media Library. You can override {@link #readImage(FileHandle)} to plug such reader in.
 * @author Kotcrab
 * @see HighResFileChooserIconProvider
 */
public class ImgScalrFileChooserIconProvider extends CachingFileChooserIconProvider {
	private static final Color tmpColor = new Color();
	private static final int MAX_IMAGE_WIDTH = 4096;
	private static final int MAX_IMAGE_HEIGHT = 4096;

	public ImgScalrFileChooserIconProvider (FileChooser chooser) {
		super(chooser);
		System.setProperty("java.awt.headless", "true");
	}

	@Override
	protected void scheduleThumbnailGeneration (final Thumbnail thumbnail, final FileChooser.ViewMode viewMode, final float thumbSize, final FileChooser.FileItem item) {
		executor.execute(new Runnable() {
			@Override
			public void run () {
				try {
					FileHandle file = item.getFile();
					ImgScalrFileChooserIconProvider.ImageInfo imageInfo = new ImgScalrFileChooserIconProvider.ImageInfo(file);
					if (imageInfo.width > MAX_IMAGE_WIDTH || imageInfo.height > MAX_IMAGE_HEIGHT)
						return;

					if (imageInfo.width < thumbSize || imageInfo.height < thumbSize) {
						updateItemImageFromFile(thumbnail, viewMode, item);
						return;
					}

					final BufferedImage imageFile = readImage(file);
					final BufferedImage scaledImg = Scalr.resize(imageFile, Scalr.Method.BALANCED, Scalr.Mode.AUTOMATIC, (int) thumbSize);

					FileHandle tmpThumbFile = null;
					if (scaledImg.getType() != BufferedImage.TYPE_INT_RGB && scaledImg.getType() != BufferedImage.TYPE_INT_ARGB) {
						tmpThumbFile = FileHandle.tempFile("filechooser");
						ImageIO.write(scaledImg, "png", tmpThumbFile.file());
					}
					updateItemImageFromScaled(thumbnail, viewMode, item, scaledImg, tmpThumbFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Reads image from file into BufferedImage. Override this if you want to add support for CMYK JPGs or want to provide
	 * non standard image loading method. For example via AWT Toolkit.
	 * @param file image file
	 * @return buffered image read from file
	 */
	protected BufferedImage readImage (FileHandle file) throws IOException {
		return ImageIO.read(file.file());
	}

	private void updateItemImageFromScaled (final Thumbnail thumbnail, final FileChooser.ViewMode viewMode,
											final FileChooser.FileItem item, final BufferedImage scaledImg, final FileHandle thumbFile) {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run () {
				try {
					Texture texture;
					if (thumbFile == null) {
						Pixmap pixmap = imageToPixmap(scaledImg);
						texture = new Texture(pixmap);
						pixmap.dispose();
					} else {
						texture = new Texture(thumbFile);
					}

					thumbnail.addThumb(viewMode, texture);
					item.setIcon(thumbnail.getThumbnail(viewMode), Scaling.fit);
				} catch (GdxRuntimeException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void updateItemImageFromFile (final Thumbnail thumbnail, final FileChooser.ViewMode viewMode,
										  final FileChooser.FileItem item) {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run () {
				try {
					Texture texture = new Texture(item.getFile());
					thumbnail.addThumb(viewMode, texture);
					item.setIcon(thumbnail.getThumbnail(viewMode), Scaling.fit);
				} catch (GdxRuntimeException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Pixmap imageToPixmap (BufferedImage image) {
		final int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		final int width = image.getWidth();
		final int height = image.getHeight();
		final boolean hasAlphaChannel = image.getAlphaRaster() != null;

		Pixmap pixmap = new Pixmap(width, height, hasAlphaChannel ? Pixmap.Format.RGBA8888 : Pixmap.Format.RGB888);

		if (hasAlphaChannel) {
			for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel++) {

				Color.argb8888ToColor(tmpColor, pixels[pixel]);
				pixmap.drawPixel(col, row, Color.rgba8888(tmpColor));

				col++;
				if (col == width) {
					col = 0;
					row++;
				}
			}
		} else {
			for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel++) {
				int color = pixels[pixel];
				tmpColor.r = ((color & 0x00ff0000) >>> 16) / 255f;
				tmpColor.g = ((color & 0x0000ff00) >>> 8) / 255f;
				tmpColor.b = ((color & 0x000000ff)) / 255f;
				tmpColor.a = 1f;

				pixmap.drawPixel(col, row, Color.rgba8888(tmpColor));

				col++;
				if (col == width) {
					col = 0;
					row++;
				}
			}
		}

		return pixmap;
	}

	@Override
	public void dispose () {
		super.dispose();
	}

	private static class ImageInfo {
		private int height;
		private int width;

		public ImageInfo (FileHandle file) {
			process(file);
		}

		public void process (FileHandle file) {
			width = -1;
			height = -1;
			InputStream is = null;
			try {
				is = new FileInputStream(file.file());
				processStream(is);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				StreamUtils.closeQuietly(is);
			}
		}

		private void processStream (InputStream is) throws IOException {
			int c1 = is.read();
			int c2 = is.read();
			int c3 = is.read();

			if (c1 == 0xFF && c2 == 0xD8) { // JPG
				while (c3 == 255) {
					int marker = is.read();
					int len = readInt(is, 2, true);
					if (marker == 192 || marker == 193 || marker == 194) {
						is.skip(1);
						height = readInt(is, 2, true);
						width = readInt(is, 2, true);
						break;
					}
					is.skip(len - 2);
					c3 = is.read();
				}
			} else if (c1 == 137 && c2 == 80 && c3 == 78) { // PNG
				is.skip(15);
				width = readInt(is, 2, true);
				is.skip(2);
				height = readInt(is, 2, true);
			} else if (c1 == 66 && c2 == 77) { // BMP
				is.skip(15);
				width = readInt(is, 2, false);
				is.skip(2);
				height = readInt(is, 2, false);
			}

		}

		private int readInt (InputStream is, int noOfBytes, boolean bigEndian) throws IOException {
			int ret = 0;
			int sv = bigEndian ? ((noOfBytes - 1) * 8) : 0;
			int cnt = bigEndian ? -8 : 8;
			for (int i = 0; i < noOfBytes; i++) {
				ret |= is.read() << sv;
				sv += cnt;
			}
			return ret;
		}
	}
}
