package us.paperlesstech.handlers

import java.awt.Graphics2D
import java.awt.Image
import java.awt.RenderingHints
import javax.imageio.ImageIO
import us.paperlesstech.MimeType
import us.paperlesstech.PreviewImage
import us.paperlesstech.helpers.ImageHelpers

class PngHandlerService extends Handler {
	static handlerFor = MimeType.PNG
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
		super.print(input) //To change body of overridden methods use File | Settings | File Templates.
	}

	@Override
	@InterceptHandler
	void sign(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)
		def signatureData = input.signature

		assert signatureData, "This method requires a signature"

		log.info "Updating the images for document ${d}"

		Image original = ImageIO.read(new ByteArrayInputStream(data.data))
		Image signature = ImageIO.read(new ByteArrayInputStream(signatureData))
		signature = signature.getScaledInstance(original.width, original.height, Image.SCALE_SMOOTH)

		Graphics2D buffer = original.createGraphics()
		buffer.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
		buffer.drawImage(original, null, null)
		buffer.drawImage(signature, null, null)
		buffer.dispose()

		ByteArrayOutputStream output = new ByteArrayOutputStream()
		ImageIO.write(original, "png", output)
		// TODO this replaces the signature in place unlike the document ones, is this okay
		data.data = output.toByteArray()
	}
}
