package us.paperlesstech.helpers

import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage

import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.stream.ImageInputStream
import javax.imageio.stream.MemoryCacheImageInputStream

import us.paperlesstech.MimeType
import us.paperlesstech.PartyColor

class ImageHelpers {
	static final String LINEBREAK = 'LB'

	/**
	 * Draw lines on the passed in BufferedImage.
	 * 
	 * @param signature A signature object. The lines should be formatted as
	 * <pre>
	 * 		line = {
	 * 			a: {
	 * 				x: Integer,
	 * 				y: Integer
	 * 			},
	 * 			b: {
	 * 				x: Integer,
	 * 				y: Integer
	 * 			}
	 * 		}
	 * </pre>		
	 */
	static void drawLines(BufferedImage image, Map signature) {
		def top = signature.top as int
		def left = signature.left as int
		def color = Color.BLACK
		
		try {
			color = PartyColor.valueOf(signature.color?.toLowerCase()?.capitalize() ?: "Black").color
		} catch (IllegalArgumentException iae) {}
		
		Graphics2D buffer = image.createGraphics()
		buffer.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
		buffer.setColor(color)

		signature.lines.each {
			if (it != LINEBREAK) {
				buffer.drawLine((it.a.x as int) + left, (it.a.y as int) + top, (it.b.x as int) + left, (it.b.y as int) + top)
			}
		}

		buffer.dispose()
	}

	/**
	 * Returns the width and height of the given image.  NOTE: The passed InputStream will NOT be closed
	 *
	 * @param is InputStream of the image to parse
	 * @param mimeType mimeType of the InputStream
	 *
	 * @return The width and height of the image in a list
	 */
	static def getDimensions(InputStream is, MimeType mimeType) {
		String suffix = mimeType.downloadExtension.substring(1)
		Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix)

		if (iter.hasNext()) {
			ImageReader reader = iter.next()
			ImageInputStream iis = new MemoryCacheImageInputStream(is)

			try {
				reader.setInput(iis)
				int width = reader.getWidth(reader.getMinIndex())
				int height = reader.getHeight(reader.getMinIndex())

				return [width, height]
			} finally {
				iis?.close()
				reader?.dispose()
			}
		}
	}

	static BufferedImage createBlankImage(width, height, background=Color.white) {
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
		Graphics2D g2d = bufferedImage.createGraphics()

		g2d.setColor(background)
		g2d.fillRect(0, 0, width, height)

		bufferedImage
	}
}
