package us.paperlesstech.handlers

import java.awt.image.BufferedImage
import java.awt.image.IndexColorModel
import java.awt.image.RenderedImage

import javax.imageio.ImageIO
import javax.media.jai.JAI
import javax.media.jai.LookupTableJAI
import javax.media.jai.NullOpImage
import javax.media.jai.OpImage
import javax.media.jai.PlanarImage

import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType
import us.paperlesstech.PreviewImage
import us.paperlesstech.helpers.ImageHelpers

import com.sun.media.jai.codec.ByteArraySeekableStream
import com.sun.media.jai.codec.ImageCodec
import com.sun.media.jai.codec.ImageDecoder
import com.sun.media.jai.codec.ImageEncoder
import com.sun.media.jai.codec.SeekableStream
import com.sun.media.jai.codec.TIFFDecodeParam
import com.sun.media.jai.codec.TIFFEncodeParam

class TiffHandlerService extends Handler {
	static final handlerFor = [MimeType.TIFF]
	static transactional = true
	def handlerChain

	@Override
	void generatePreview(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)

		d.resetPreviewImages()

		TIFFDecodeParam param = null
		byte[] bytes = fileService.getBytes(data)
		SeekableStream s = new ByteArraySeekableStream(bytes)
		ImageDecoder dec = ImageCodec.createImageDecoder("tiff", s, param)

		(1..data.pages).each { i ->
			ByteArrayOutputStream os = new ByteArrayOutputStream()
			// TIFF images are 0 indexed
			RenderedImage original = dec.decodeAsRenderedImage(i - 1)

			ImageIO.write(original, "png", os)

			byte[] pngBytes = os.toByteArray()
			def (width, height) =  ImageHelpers.getDimensions(new ByteArrayInputStream(pngBytes), MimeType.PNG)
			DocumentData newPng = fileService.createDocumentData(bytes: pngBytes, mimeType: MimeType.PNG)
			PreviewImage image = new PreviewImage(data: newPng, sourceHeight: height, pageNumber: i, sourceWidth: width)
			d.addToPreviewImages(image)
		}

		assert d.previewImages.size() == data.pages
	}

	@Override
	void importFile(Map input) {
		def d = getDocument(input)

		TIFFDecodeParam param = null
		SeekableStream s = new ByteArraySeekableStream(input.bytes)
		ImageDecoder dec = ImageCodec.createImageDecoder("tiff", s, param)

		int pages = dec.getNumPages()

		def data = fileService.createDocumentData(mimeType: MimeType.TIFF, bytes: input.bytes, pages: pages)
		d.addToFiles(data)
		input.bytes = null

		handlerChain.generatePreview(document: d, documentData: data)

		assert d.files.size() == 1
		assert d.previewImages.size() == d.files.first().pages
	}

	@Override
	void cursiveSign(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)
		def signatureData = input.signatures

		assert signatureData, "This method requires a signature"

		log.info "Updating the images for document ${d}"

		TIFFDecodeParam param = null
		SeekableStream s = new ByteArraySeekableStream(fileService.getBytes(data))
		ImageDecoder dec = ImageCodec.createImageDecoder("tiff", s, param)

		def images = []
		(1..data.pages).each {i ->
			// TIFF images are 0 indexed
			PlanarImage planar = new NullOpImage(dec.decodeAsRenderedImage((i as int) - 1),  null, OpImage.OP_IO_BOUND, null)

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

				planar = JAI.create("lookup", planar, new LookupTableJAI(bytes))
			}
			
			BufferedImage original = planar.getAsBufferedImage()

			if (signatureData[i as String]) {
				ImageHelpers.drawLines(original, signatureData[i as String])
			}

			images += original
		}

		TIFFEncodeParam params = new TIFFEncodeParam()
		OutputStream os = new ByteArrayOutputStream()
		ImageEncoder encoder = ImageCodec.createImageEncoder("tiff", os, params);

		params.setExtraImages(images[1..<data.pages].iterator())
		params.setCompression(TIFFEncodeParam.COMPRESSION_DEFLATE)
		encoder.encode(images[0]);
		
		DocumentData newTiff = fileService.createDocumentData(bytes: os.toByteArray(), mimeType: data.mimeType, pages: data.pages)
		d.addToFiles(newTiff)
		input.documentData = newTiff
		handlerChain.generatePreview(input)
	}
}
