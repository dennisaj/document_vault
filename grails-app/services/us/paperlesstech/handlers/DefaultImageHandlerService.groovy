package us.paperlesstech.handlers

import java.awt.Image

import javax.imageio.ImageIO

import org.springframework.core.io.ClassPathResource

import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType
import us.paperlesstech.PreviewImage
import us.paperlesstech.helpers.FileHelpers
import us.paperlesstech.helpers.ImageHelpers

class DefaultImageHandlerService extends Handler {
	static final handlerFor = [MimeType.BMP, MimeType.GIF, MimeType.JPEG, MimeType.PNG]
	static transactional = true
	static imagethumbnail = new ClassPathResource("scripts/imagethumbnail.sh").file.absolutePath

	def handlerChain

	@Override
	void generatePreview(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)

		d.resetPreviewImages()

		def (width, height) = fileService.withInputStream(data) { is ->
			ImageHelpers.getDimensions(is, data.mimeType)
		}

		String imagePath = fileService.getAbsolutePath(data)
		String baseName = FileHelpers.chopExtension(imagePath)

		def cmd = """/bin/bash $imagethumbnail $imagePath"""
		log.debug "PreviewImage create - $cmd"
		def proc = cmd.execute()
		proc.waitFor()

		if (proc.exitValue()) {
			throw new RuntimeException("""Unable to generate preview for document ${d.id} - PDF to PNG conversion failed. Command: $cmd""")
		}

		File thumbnail = new File("$baseName-thumbnail.png")
		assert thumbnail.canRead(), "Didn't generate thumbnail for $d"

		byte[] thumbnailBytes = thumbnail.bytes
		thumbnail.delete()

		PreviewImage image = new PreviewImage(data: data, sourceHeight: height, pageNumber: 1, sourceWidth: width)
		image.thumbnail = fileService.createDocumentData(mimeType:MimeType.PNG, bytes:thumbnailBytes)
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

		signatureData.each { signature ->
			ImageHelpers.drawLines(original, signature)
		}

		ByteArrayOutputStream output = new ByteArrayOutputStream()
		ImageIO.write(original, data.mimeType.toString().toLowerCase(), output)

		DocumentData newImage = fileService.createDocumentData(bytes: output.toByteArray(), mimeType: data.mimeType, pages: data.pages)
		d.addToFiles(newImage)
		input.documentData = newImage
		handlerChain.generatePreview(input)
	}

	@Override
	void clickWrap(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)
		def highlights = input.highlights

		assert highlights, "This method requires a highlight"

		log.info "clickWraping the images for document ${d}"

		Image original
		fileService.withInputStream(data) { is ->
			original = ImageIO.read(is)
		}

		ImageHelpers.addNotesToImage(original, highlights)

		ByteArrayOutputStream output = new ByteArrayOutputStream()
		ImageIO.write(original, data.mimeType.toString().toLowerCase(), output)

		DocumentData newImage = fileService.createDocumentData(bytes: output.toByteArray(), mimeType: data.mimeType, pages: data.pages)
		d.addToFiles(newImage)
		input.documentData = newImage
		handlerChain.generatePreview(input)
	}
}
