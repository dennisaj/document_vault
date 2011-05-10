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
	def nextService

	@Override
	void generatePreview(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)

		d.resetPreviewImages()

		def (width, height) = ImageHelpers.getDimensions(data.data)
		PreviewImage image = new PreviewImage(data: data, height: height, pageNumber: 1, width: width)
		d.addToPreviewImages(image)
	}

	@Override
	void importFile(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)

		d.addToFiles(data)
		handlerChain.generatePreview(input)
	}

	@Override
	void sign(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)
		// By default, images only have one page
		def signatureData = input.signatures["1"]

		assert signatureData, "This method requires a signature"

		log.info "Updating the images for document ${d}"

		Image original = ImageIO.read(new ByteArrayInputStream(data.data))

		ImageHelpers.drawLines(original, signatureData)

		ByteArrayOutputStream output = new ByteArrayOutputStream()
		ImageIO.write(original, data.mimeType.toString().toLowerCase(), output)

		DocumentData newImage = new DocumentData(data: output.toByteArray(), mimeType: data.mimeType, pages: data.pages)
		d.addToFiles(newImage)
		input.documentData = newImage
		handlerChain.generatePreview(input)
	}
}
