package us.paperlesstech.handlers

import java.awt.Image

import javax.imageio.ImageIO

import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType
import us.paperlesstech.PreviewImage
import us.paperlesstech.helpers.ImageHelpers

class DefaultImageHandlerService extends Handler {
	static final handlerFor = [MimeType.BMP, MimeType.GIF, MimeType.JPEG, MimeType.PNG]
	static transactional = true
	def handlerChain

	@Override
	void generatePreview(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)

		d.resetPreviewImages()

		def (width, height) = fileService.withInputStream(data) { is ->
			ImageHelpers.getDimensions(is, data.mimeType)
		}
		PreviewImage image = new PreviewImage(data: data, height: height, pageNumber: 1, width: width)
		d.addToPreviewImages(image)
	}

	@Override
	void importFile(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)

		data = fileService.createDocumentData(mimeType: data.mimeType, bytes: input.bytes)
		d.addToFiles(data)
		input.bytes = null

		handlerChain.generatePreview(document: d, documentData: data)

		assert d.files.size() == 1
		assert d.previewImages.size() == 1
	}

	@Override
	void cursiveSign(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)
		// By default, images only have one page
		def signatureData = input.signatures["1"]

		assert signatureData, "This method requires a signature"

		log.info "Updating the images for document ${d}"

		Image original
		fileService.withInputStream(data) { is ->
			original = ImageIO.read(is)
		}

		ImageHelpers.drawLines(original, signatureData)

		ByteArrayOutputStream output = new ByteArrayOutputStream()
		ImageIO.write(original, data.mimeType.toString().toLowerCase(), output)

		DocumentData newImage = fileService.createDocumentData(bytes: output.toByteArray(), mimeType: data.mimeType, pages: data.pages)
		d.addToFiles(newImage)
		input.documentData = newImage
		handlerChain.generatePreview(input)
	}
}
