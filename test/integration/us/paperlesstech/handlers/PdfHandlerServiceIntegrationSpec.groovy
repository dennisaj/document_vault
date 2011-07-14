package us.paperlesstech.handlers

import org.springframework.core.io.ClassPathResource
import us.paperlesstech.Document
import us.paperlesstech.DomainIntegrationSpec
import us.paperlesstech.MimeType
import us.paperlesstech.DocumentData

class PdfHandlerServiceIntegrationSpec extends BaseHandlerSpec {
	def fileService
	def pdfHandlerService
	def document
	def pdfDocumentData
	def pdfBytes = new ClassPathResource("2pages.pdf").getFile().bytes
	def line = [a: [x: 0, y: 0], b: [x: 100, y: 100]]

	@Override
	def setup() {
		pdfHandlerService.authServiceProxy = authServiceProxy
		document = new Document()
		document.group = DomainIntegrationSpec.group
		pdfDocumentData = new DocumentData(mimeType: MimeType.PDF)
	}

	def "pdf2png should be set"() {
		expect:
			PdfHandlerService.pdf2png
	}

	def "import pdf file"() {
		def input = [document: document, documentData: pdfDocumentData, bytes: pdfBytes]
		when:
		pdfHandlerService.importFile(input)

		then:
		document.files.first().pages == 2
		document.files.first().mimeType == MimeType.PDF
		document.previewImages.size() == 2
		document.previewImage(1).data.pages == 1
		document.previewImage(1).data.mimeType == MimeType.PNG
		document.previewImage(2).data.pages == 1
		document.previewImage(2).data.mimeType == MimeType.PNG
		document.previewImage(1).thumbnail.pages == 1
		document.previewImage(1).thumbnail.mimeType == MimeType.PNG
		document.previewImage(2).thumbnail.pages == 1
		document.previewImage(2).thumbnail.mimeType == MimeType.PNG
	}

	def "cursiveSign pdf file"() {
		given:
		def lines = ['1': [line, 'LB'], '2': [line], '4': [line]]
		when:
		def document = new Document(group: DomainIntegrationSpec.group)
		def input = [document: document, documentData: pdfDocumentData, bytes: pdfBytes]

		pdfHandlerService.importFile(input)
		document = document.save()
		input = [document: document, documentData: document.files.first(), signatures: lines]
		pdfHandlerService.cursiveSign(input)

		then:
		document.files.size() == 2
		document.files.first().pages == 2
		document.files.first().mimeType == mimeType
		document.files.first().fileKey != document.files.last().fileKey
		document.previewImages.size() == 2
		document.previewImages*.data.pages == [1] * 2
		document.previewImages*.data.mimeType == [MimeType.PNG] * 2
		document.previewImages*.thumbnail.pages == [1] * 2
		document.previewImages*.thumbnail.mimeType == [MimeType.PNG] * 2
		document.previewImages*.pageNumber == [1, 2]
		where:
		mimeType = MimeType.PDF
	}
}
