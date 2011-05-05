package us.paperlesstech.handlers

import java.awt.Graphics2D
import java.awt.Image
import java.awt.RenderingHints

import javax.imageio.ImageIO

import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType
import us.paperlesstech.PreviewImage
import us.paperlesstech.helpers.ImageHelpers

class PngHandlerService extends Handler {
	static handlerFor = MimeType.PNG
	static LINEBREAK = 'LINEBREAK'
	static transactional = true
	def handlerChain
	def nextService

	@Override
	void generatePreview(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)

		d.resetPreviewImages()

		def (width, height) = ImageHelpers.getDimensions(data.data)
		PreviewImage image = new PreviewImage(data: data.clone(), height: height, pageNumber: 1, width: width)
		d.addToPreviewImages(image)
	}

	@Override
	@InterceptHandler
	void importFile(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)

		d.addToFiles(data)
		handlerChain.generatePreview(input)
	}

	@Override
	void print(Map input) {
	}

	@Override
	@InterceptHandler
	void sign(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)
		// PNGs only have one page
		def signatureData = input.signatures["1"]

		assert signatureData, "This method requires a signature"

		log.info "Updating the images for document ${d}"

		Image original = ImageIO.read(new ByteArrayInputStream(data.data))

		Graphics2D buffer = original.createGraphics()
		buffer.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
		buffer.setColor(java.awt.Color.BLACK)

		signatureData.each {
			if (it != LINEBREAK) {
				buffer.drawLine(it.start.x as int, it.start.y as int, it.end.x as int, it.end.y as int)
			}
		}

		buffer.dispose()

		ByteArrayOutputStream output = new ByteArrayOutputStream()
		ImageIO.write(original, "png", output)

		DocumentData newPng = new DocumentData(mimeType: data.mimeType, pages: data.pages)
		newPng.data = output.toByteArray()
		d.addToFiles(newPng)
		input.documentData = newPng
		handlerChain.generatePreview(input)
	}
}
