package us.paperlesstech.helpers

import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage

import javax.imageio.ImageIO

import com.thebuzzmedia.imgscalr.Scalr

class ImageHelpers {
	static final String LINEBREAK = 'LINEBREAK'

	/**
	 * Draw lines on the passed in BufferedImage.
	 * 
	 * @param lines A list of maps. The maps should be formatted as
	 * <pre>
	 * 		line = {
	 * 			start: {
	 * 				x: a,
	 * 				y: b
	 * 			},
	 * 			end: {
	 * 				x: c,
	 * 				y: d
	 * 			}
	 * 		}
	 * </pre>		
	 */
	static void drawLines(BufferedImage image, List lines) {
		Graphics2D buffer = image.createGraphics()
		buffer.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
		buffer.setColor(java.awt.Color.BLACK)

		lines.each {
			if (it != LINEBREAK) {
				buffer.drawLine(it.start.x as int, it.start.y as int, it.end.x as int, it.end.y as int)
			}
		}

		buffer.dispose()
	}
	
	/**
	 * Returns the width and height of the given image
	 *
	 * @param input the image to parse
	 *
	 * @return The width and height of the image in a list
	 */
	static def getDimensions(byte[] input) {
		def is = new ByteArrayInputStream(input)
		def original = ImageIO.read(is)
		is.close()

		[original.width, original.height]
	}

	/**
	 * Scales the image to the given size
	 *
	 * @param input A byte array which should be an image format recognized by Java.
	 *
	 * @param finalWidth The width of the image after scaling
	 * @param finalHeight The height of the image after scaling
	 * @param outputFormat The format of the resulting image, defaults to PNG.
	 *
	 * @return A byte[] of the image scaled to the given size
	 */
	static byte[] scaleImage(byte[] input, int finalWidth, int finalHeight, String outputFormat = "png") {
		def is = new ByteArrayInputStream(input)
		def original = ImageIO.read(is)
		is.close()

		def scaled = Scalr.resize(original, com.thebuzzmedia.imgscalr.Scalr.Method.QUALITY, finalWidth, finalHeight)

		ByteArrayOutputStream output = new ByteArrayOutputStream()
		ImageIO.write(scaled, outputFormat, output)

		byte[] bytes = output.toByteArray()
		output.close()

		bytes
	}
}
