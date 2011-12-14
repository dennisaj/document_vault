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
	def signature
	def line = [a: [x: 0, y: 0], b: [x: 100, y: 100]]

	def setup() {
		signature = [height: 150, width: 750, top: 0, left: 0, lines:[line, 'LB']]
		tiffDocument = new Document()
		tiffDocument.group = DomainIntegrationSpec.group
		tiffDocumentData = new DocumentData(mimeType: MimeType.TIFF)
	}

	def "imagethumbnail should be set"() {
		expect:
			TiffHandlerService.imagethumbnail
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
		tiffDocument.previewImages*.thumbnail.pages == [1] * 6
		tiffDocument.previewImages*.thumbnail.mimeType == [MimeType.PNG] * 6
		tiffDocument.previewImages*.pageNumber == 1..6
	}

	def "cursiveSign a tiff"() {
		when:
		tiffHandlerService.importFile(document: tiffDocument, documentData: tiffDocumentData, bytes: tiffBytes)
		tiffDocument.save()
		tiffHandlerService.cursiveSign(document: tiffDocument, documentData: tiffDocument.files.first(), signatures: ["1":[signature], "2":[signature]])

		then:
		tiffDocument.files.size() == 2
		tiffDocument.files.first().pages == 6
		tiffDocument.files.first().mimeType == MimeType.TIFF
		tiffDocument.files.first().fileKey != tiffDocument.files.last().fileKey
		tiffDocument.previewImages.size() == 6
		tiffDocument.previewImages*.data.pages == [1] * 6
		tiffDocument.previewImages*.data.mimeType == [MimeType.PNG] * 6
		tiffDocument.previewImages*.thumbnail.pages == [1] * 6
		tiffDocument.previewImages*.thumbnail.mimeType == [MimeType.PNG] * 6
		tiffDocument.previewImages*.pageNumber == 1..6
	}
}
