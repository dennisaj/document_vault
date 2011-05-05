package us.paperlesstech.handlers

import grails.plugin.spock.IntegrationSpec

import org.springframework.core.io.ClassPathResource

import us.paperlesstech.Document
import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType

class PdfHandlerServiceIntegrationSpec extends IntegrationSpec {
	def pdfHandlerService
	def document
	def pdfData

	def setup() {
		document = new Document()
		pdfData = new DocumentData(mimeType: MimeType.PDF)
		pdfData.data = new ClassPathResource("2pages.pdf").getFile().getBytes()
	}

	def "import pdf file"() {
		def input = [document: document, documentData: pdfData]
		when:
		pdfHandlerService.importFile(input)

		then:
		document.files.first().pages == 2
		document.files.first().mimeType == MimeType.PDF
		document.files.first().data == pdfData.data
		document.previewImages.size() == 2
		document.previewImage(1).data.pages == 1
		document.previewImage(1).data.mimeType == MimeType.PNG
		document.previewImage(2).data.pages == 1
		document.previewImage(2).data.mimeType == MimeType.PNG
	}
}
