package us.paperlesstech.handlers

import java.awt.Graphics2D
import java.awt.Image
import java.awt.RenderingHints
import javax.imageio.ImageIO
import us.paperlesstech.MimeType

class PngHandlerService extends Handler {
	static handlerFor = MimeType.PNG
	static transactional = true

	@Override
	@InterceptHandler
	void sign(Map inputs) {
		def data = getDocumentData(inputs)
		def signatureData = inputs.signature

		assert signature, "This method requires a signature"

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
