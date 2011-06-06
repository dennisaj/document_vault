package us.paperlesstech.handlers

import grails.plugin.spock.IntegrationSpec

import org.springframework.core.io.ClassPathResource

import us.paperlesstech.Document
import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType
import us.paperlesstech.DomainIntegrationSpec

class PdfHandlerServiceIntegrationSpec extends BaseHandlerSpec {
	def pdfHandlerService
	def document
	def pdfData
	def line =  [a:[x:0,y:0], b:[x:100,y:100]]

	def setup() {
		document = new Document()
		document.group = DomainIntegrationSpec.group
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

	def "sign pdf file"() {
		given:
		def lines = ['1': [line, 'LB'], '2': [line], '4': [line]]
		when:
		def document = new Document(group: DomainIntegrationSpec.group)
		def documentData = new DocumentData(data: new ClassPathResource("2pages.pdf").getFile().getBytes(),
				mimeType: mimeType)
		def input = [document: document, documentData: documentData]

		pdfHandlerService.importFile(input)
		document = document.save()
		input = [document: document, documentData: document.files.first(), signatures: lines]
		pdfHandlerService.sign(input)

		then:
		document.files.size() == 2
		document.files.first().pages == 2
		document.files.first().mimeType == mimeType
		document.files.first().data != document.files.last().data
		document.previewImages.size() == 2
		document.previewImages*.data.pages == [1] * 2
		document.previewImages*.data.mimeType == [MimeType.PNG] * 2
		document.previewImages*.pageNumber == [1, 2]
		document.signed == true
		where:
		mimeType = MimeType.PDF
	}
}
