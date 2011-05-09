package us.paperlesstech.handlers

import grails.plugin.spock.IntegrationSpec

import org.springframework.core.io.ClassPathResource

import us.paperlesstech.Document
import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType

class DefaultImageHandlerServiceIntegrationSpec extends IntegrationSpec {
	def defaultImageHandlerService
	def pngDocument
	def jpegDocument
	def bmpDocument
	def gifDocument
	def pngData
	def jpegData
	def bmpData
	def gifData

	def setup() {
		pngDocument = new Document()
		pngData = new DocumentData(mimeType: MimeType.PNG)
		pngData.data = new ClassPathResource("test.png").getFile().getBytes()

		jpegDocument = new Document()
		jpegData = new DocumentData(mimeType: MimeType.JPEG)
		jpegData.data = new ClassPathResource("test.jpg").getFile().getBytes()

		bmpDocument = new Document()
		bmpData = new DocumentData(mimeType: MimeType.BMP)
		bmpData.data = new ClassPathResource("test.bmp").getFile().getBytes()

		gifDocument = new Document()
		gifData = new DocumentData(mimeType: MimeType.GIF)
		gifData.data = new ClassPathResource("test.gif").getFile().getBytes()
	}

	def "import png file"() {
		def input = [document: pngDocument, documentData: pngData]
		when:
		defaultImageHandlerService.importFile(input)

		then:
		pngDocument.files.first().pages == 1
		pngDocument.files.first().mimeType == MimeType.PNG
		pngDocument.files.first().data == pngData.data
		pngDocument.previewImages.size() == 1
		pngDocument.previewImage(1).data.pages == 1
		pngDocument.previewImage(1).data.mimeType == MimeType.PNG
	}

	def "import jpeg file"() {
		def input = [document: jpegDocument, documentData: jpegData]
		when:
		defaultImageHandlerService.importFile(input)

		then:
		jpegDocument.files.first().pages == 1
		jpegDocument.files.first().mimeType == MimeType.JPEG
		jpegDocument.files.first().data == jpegData.data
		jpegDocument.previewImages.size() == 1
		jpegDocument.previewImage(1).data.pages == 1
		jpegDocument.previewImage(1).data.mimeType == MimeType.JPEG
	}

	def "import bmp file"() {
		def input = [document: bmpDocument, documentData: bmpData]
		when:
		defaultImageHandlerService.importFile(input)

		then:
		bmpDocument.files.first().pages == 1
		bmpDocument.files.first().mimeType == MimeType.BMP
		bmpDocument.files.first().data == bmpData.data
		bmpDocument.previewImages.size() == 1
		bmpDocument.previewImage(1).data.pages == 1
		bmpDocument.previewImage(1).data.mimeType == MimeType.BMP
	}

	def "import gif file"() {
		def input = [document: gifDocument, documentData: gifData]
		when:
		defaultImageHandlerService.importFile(input)

		then:
		gifDocument.files.first().pages == 1
		gifDocument.files.first().mimeType == MimeType.GIF
		gifDocument.files.first().data == gifData.data
		gifDocument.previewImages.size() == 1
		gifDocument.previewImage(1).data.pages == 1
		gifDocument.previewImage(1).data.mimeType == MimeType.GIF
	}
}
