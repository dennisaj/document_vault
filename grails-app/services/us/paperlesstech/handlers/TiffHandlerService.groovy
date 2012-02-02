package us.paperlesstech.handlers

import java.awt.image.RenderedImage

import javax.imageio.ImageIO

import org.springframework.core.io.ClassPathResource

import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType
import us.paperlesstech.PreviewImage
import us.paperlesstech.helpers.FileHelpers
import us.paperlesstech.helpers.ImageHelpers

import com.sun.media.jai.codec.ByteArraySeekableStream
import com.sun.media.jai.codec.ImageCodec
import com.sun.media.jai.codec.ImageDecoder
import com.sun.media.jai.codec.SeekableStream
import com.sun.media.jai.codec.TIFFDecodeParam

class TiffHandlerService extends Handler {
	static final handlerFor = [MimeType.TIFF]
	static transactional = true
	static imagethumbnail = new ClassPathResource("scripts/imagethumbnail.sh").file.absolutePath

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

			String imagePath = fileService.getAbsolutePath(newPng)
			String baseName = FileHelpers.chopExtension(imagePath)

			// TODO: An optimization would be to convert all pages to thumbnails at one time instead of one at a time.
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

			PreviewImage image = new PreviewImage(data: newPng, sourceHeight: height, pageNumber: i, sourceWidth: width)
			image.thumbnail = fileService.createDocumentData(mimeType:MimeType.PNG, bytes:thumbnailBytes)

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

		def bytes = ImageHelpers.doWithEachTiffPage(fileService.getBytes(data)) { i, original->
			signatureData[(i + 1).toString()].each { signature ->
				ImageHelpers.drawLines(original, signature)
			}
		}

		DocumentData newTiff = fileService.createDocumentData(bytes: bytes, mimeType: data.mimeType, pages: data.pages)
		d.addToFiles(newTiff)
		input.documentData = newTiff
		handlerChain.generatePreview(input)
	}

	@Override
	void clickWrap(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)
		def highlightsByPage = input.highlights?.groupBy { it.pageNumber }

		log.info "clickWraping the images for document ${d}"

		def bytes = ImageHelpers.doWithEachTiffPage(fileService.getBytes(data)) { i, original->
			def highlights = highlightsByPage[i + 1]
			if (highlights) {
				ImageHelpers.addNotesToImage(original, highlights)
			}
		}

		DocumentData newTiff = fileService.createDocumentData(bytes: bytes, mimeType: data.mimeType, pages: data.pages)
		d.addToFiles(newTiff)
		input.documentData = newTiff
		handlerChain.generatePreview(input)
	}
}
