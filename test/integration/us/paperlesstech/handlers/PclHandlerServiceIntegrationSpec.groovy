package us.paperlesstech.handlers

import org.springframework.core.io.ClassPathResource

import us.paperlesstech.Document

import us.paperlesstech.MimeType

import us.paperlesstech.DomainIntegrationSpec
import us.paperlesstech.DocumentData

class PclHandlerServiceIntegrationSpec extends BaseHandlerSpec {
	def fileService
	def handlerChain
	def document
	def pclDocumentData
	def pclBytes = new ClassPathResource("dt_combined.pcl").getFile().bytes

	def setup() {
		document = new Document()
		document.group = DomainIntegrationSpec.group
		pclDocumentData = new DocumentData(mimeType: MimeType.PCL)
	}

	def "import ferman pcl file"() {
		when:
		handlerChain.importFile(document: document, documentData: pclDocumentData, bytes: pclBytes)

		then:
		document.searchField("DocumentType") == "CustomerHardCopy"
		document.files.first().pages == 1
		document.files.first().mimeType == MimeType.PDF
		document.files.last().mimeType == MimeType.PCL
		document.files.first().fileKey != document.files.last().fileKey
		fileService.getBytes(document.files.first())[0..3] == "%PDF".bytes
		document.previewImages.size() == 1
		document.previewImage(1).data.pages == 1
		document.previewImage(1).data.mimeType == MimeType.PNG
	}
}
