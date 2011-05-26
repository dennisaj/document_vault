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
	def line

	def setup() {
		tiffDocument = new Document()
		tiffData = new DocumentData(mimeType: MimeType.TIFF)
		tiffData.data = new ClassPathResource("multipage.tif").getFile().getBytes()
		
		line = [a:[x:0,y:0], b:[x:100,y:100]]
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

	def "sign a tiff"() {
		given:
			def lines = ['1':[line, 'LB'], '2':[line], '4':[line]]
			def input = [document: tiffDocument, documentData: tiffData, signatures: lines]
		when:
			tiffHandlerService.importFile(input)
			tiffDocument.save()
			tiffHandlerService.sign(input)

		then:
			tiffDocument.files.size() == 2
			tiffDocument.files.first().pages == 6
			tiffDocument.files.first().mimeType == MimeType.TIFF
			tiffDocument.files.first().data != tiffDocument.files.last().data
			tiffDocument.previewImages.size() == 6
			tiffDocument.previewImages*.data.pages == [1] * 6
			tiffDocument.previewImages*.data.mimeType == [MimeType.PNG] * 6
			tiffDocument.previewImages*.pageNumber == 1..6
			tiffDocument.signed == true
	}
}
