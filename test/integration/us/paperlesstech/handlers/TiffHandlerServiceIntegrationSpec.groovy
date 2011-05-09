package us.paperlesstech.handlers

import grails.plugin.spock.IntegrationSpec

import org.springframework.core.io.ClassPathResource

import us.paperlesstech.Document
import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType

class TiffHandlerServiceIntegrationSpec extends IntegrationSpec {
	def tiffHandlerService
	def tiffDocument
	def tiffData

	def setup() {
		tiffDocument = new Document()
		tiffData = new DocumentData(mimeType: MimeType.TIFF)
		tiffData.data = new ClassPathResource("multipage.tif").getFile().getBytes()
	}

	def "import tiff file"() {
		def input = [document: tiffDocument, documentData: tiffData]
		when:
			tiffHandlerService.importFile(input)

		then:
			tiffDocument.files.first().pages == 6
			tiffDocument.files.first().mimeType == MimeType.TIFF
			tiffDocument.files.first().data == tiffData.data
			tiffDocument.previewImages.size() == 6
			tiffDocument.previewImages*.data.pages == [1] * 6
			tiffDocument.previewImages*.data.mimeType == [MimeType.PNG] * 6
			tiffDocument.previewImages*.pageNumber == 1..6
	}
}
