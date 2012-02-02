package us.paperlesstech.helpers

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.font.FontRenderContext
import java.awt.font.LineBreakMeasurer
import java.awt.font.TextLayout
import java.awt.image.BufferedImage
import java.awt.image.IndexColorModel
import java.text.AttributedCharacterIterator
import java.text.AttributedString
import java.util.Map

import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.stream.ImageInputStream
import javax.imageio.stream.MemoryCacheImageInputStream
import javax.media.jai.JAI
import javax.media.jai.LookupTableJAI
import javax.media.jai.NullOpImage
import javax.media.jai.OpImage
import javax.media.jai.PlanarImage

import com.sun.media.jai.codec.ByteArraySeekableStream
import com.sun.media.jai.codec.ImageCodec
import com.sun.media.jai.codec.ImageDecoder
import com.sun.media.jai.codec.ImageEncoder
import com.sun.media.jai.codec.SeekableStream
import com.sun.media.jai.codec.TIFFDecodeParam
import com.sun.media.jai.codec.TIFFEncodeParam

import us.paperlesstech.MimeType
import us.paperlesstech.PartyColor

class ImageHelpers {
	private static final Font font = new Font("Helvetica", Font.PLAIN, 12)
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
	public static void drawLines(BufferedImage image, Map signature) {
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
	public static def getDimensions(InputStream is, MimeType mimeType) {
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

	public static BufferedImage createBlankImage(width, height, background=Color.white) {
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
		Graphics2D graphics = bufferedImage.createGraphics()

		graphics.setColor(background)
		graphics.fillRect(0, 0, width, height)
		graphics.dispose()

		bufferedImage
	}

	public static void addNotesToImage(BufferedImage image, List notes) {
		Graphics2D graphics = image.createGraphics()
		graphics.setFont(font)
		graphics.setColor(Color.black)
		FontRenderContext frc = graphics.getFontRenderContext()

		notes.each {
			AttributedString styledText = new AttributedString(it.note)
			AttributedCharacterIterator iterator = styledText.getIterator()
			LineBreakMeasurer measurer = new LineBreakMeasurer(iterator, frc)
			measurer.setPosition(iterator.getBeginIndex())

			float left = it.left
			float top = it.top
			while (measurer.getPosition() < iterator.getEndIndex()) {
				TextLayout layout = measurer.nextLayout(it.width)

				top += layout.getAscent()
				float dx = layout.isLeftToRight() ? 0f : it.width - layout.getAdvance()

				layout.draw(graphics, (left + dx) as float, top)
				top += layout.getDescent() + layout.getLeading()
			}
		}

		graphics.dispose()
	}

	public static byte[] doWithEachTiffPage(byte[] tiffAsBytes, Closure closure) {
		TIFFDecodeParam param = null
		SeekableStream s = new ByteArraySeekableStream(tiffAsBytes)
		ImageDecoder dec = ImageCodec.createImageDecoder("tiff", s, param)

		def images = []
		// TIFF images are 0 indexed
		(0..<dec.numPages).each { i ->
			PlanarImage planar = new NullOpImage(dec.decodeAsRenderedImage(i),  null, OpImage.OP_IO_BOUND, null)

			if (planar.getColorModel() instanceof IndexColorModel) {
				IndexColorModel icm = (IndexColorModel) planar.getColorModel()
				int numBands = icm.hasAlpha() ? 4 : 3
				byte[][] bytes = new byte[numBands][icm.getMapSize()]

				icm.getReds(bytes[0])
				icm.getGreens(bytes[1])
				icm.getBlues(bytes[2])

				if (numBands == 4) {
					icm.getAlphas(bytes[3])
				}

				planar.dispose()
				planar = JAI.create("lookup", planar, new LookupTableJAI(bytes))
			}

			BufferedImage original = planar.getAsBufferedImage()
			images << original

			closure(i, original)

			planar.dispose()
		}

		OutputStream os = new ByteArrayOutputStream()
		TIFFEncodeParam params = new TIFFEncodeParam()
		params.setExtraImages(images[1..<dec.numPages].iterator())
		params.setCompression(TIFFEncodeParam.COMPRESSION_DEFLATE)

		ImageEncoder encoder = ImageCodec.createImageEncoder("tiff", os, params)
		encoder.encode(images[0])

		os.toByteArray()
	}
}
