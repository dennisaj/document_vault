package us.paperlesstech.handlers

import org.springframework.core.io.ClassPathResource
import us.paperlesstech.Document
import us.paperlesstech.DocumentData
import us.paperlesstech.DomainIntegrationSpec
import us.paperlesstech.MimeType

class TiffHandlerServiceIntegrationSpec extends BaseHandlerSpec {
	def fileService
	def tiffHandlerService
	def tiffDocument
	def tiffDocumentData
	def tiffBytes = new ClassPathResource("multipage.tif").getFile().bytes
	def line

	def setup() {
		tiffDocument = new Document()
		tiffDocument.group = DomainIntegrationSpec.group
		tiffDocumentData = new DocumentData(mimeType: MimeType.TIFF)

		line = [a: [x: 0, y: 0], b: [x: 100, y: 100]]
	}

	def "import tiff file"() {
		def input = [document: tiffDocument, documentData: tiffDocumentData, bytes: tiffBytes]
		when:
		tiffHandlerService.importFile(input)

		then:
		tiffDocument.files.first().pages == 6
		tiffDocument.files.first().mimeType == MimeType.TIFF
		tiffDocument.previewImages.size() == 6
		tiffDocument.previewImages*.data.pages == [1] * 6
		tiffDocument.previewImages*.data.mimeType == [MimeType.PNG] * 6
		tiffDocument.previewImages*.pageNumber == 1..6
	}

	def "cursiveSign a tiff"() {
		given:
		def lines = ['1': [line, 'LB'], '2': [line], '4': [line]]
		when:
		tiffHandlerService.importFile(document: tiffDocument, documentData: tiffDocumentData, bytes: tiffBytes)
		tiffDocument.save()
		tiffHandlerService.cursiveSign(document: tiffDocument, documentData: tiffDocument.files.first(), signatures: lines)

		then:
		tiffDocument.files.size() == 2
		tiffDocument.files.first().pages == 6
		tiffDocument.files.first().mimeType == MimeType.TIFF
		tiffDocument.files.first().fileKey != tiffDocument.files.last().fileKey
		tiffDocument.previewImages.size() == 6
		tiffDocument.previewImages*.data.pages == [1] * 6
		tiffDocument.previewImages*.data.mimeType == [MimeType.PNG] * 6
		tiffDocument.previewImages*.pageNumber == 1..6
	}
}
