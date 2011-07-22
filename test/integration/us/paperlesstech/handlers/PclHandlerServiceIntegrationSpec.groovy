package us.paperlesstech.handlers

import org.springframework.core.io.ClassPathResource

import us.paperlesstech.Document

import us.paperlesstech.MimeType

import us.paperlesstech.DomainIntegrationSpec
import us.paperlesstech.DocumentData
import us.paperlesstech.helpers.PclInfo

class PclHandlerServiceIntegrationSpec extends BaseHandlerSpec {
	def fileService
	def handlerChain
	def document
	def pclDocumentData
	def pclBytes = new ClassPathResource("3pages_2_doc.pcl").getFile().bytes
	PclInfo pclInfo

	def setup() {
		document = new Document()
		document.group = DomainIntegrationSpec.group
		pclDocumentData = new DocumentData(mimeType: MimeType.PCL)
		pclInfo = new PclInfo()
		pclInfo.parse(data: pclBytes)
	}

	def "import ferman pcl file document 1"() {
		when:
		handlerChain.importFile(document: document, documentData: pclDocumentData, bytes: pclBytes,
				pclDocument: pclInfo.documents[0])

		then:
		// Just to make sure the business service was called
		document.searchField("DocumentType")
		document.files.first().pages == 1
		document.files.first().mimeType == MimeType.PDF
		document.files.last().mimeType == MimeType.PCL
		document.files.first().fileKey != document.files.last().fileKey
		fileService.getBytes(document.files.first())[0..3] == "%PDF".bytes
		document.previewImages.size() == 1
		document.previewImage(1).data.pages == 1
		document.previewImage(1).data.mimeType == MimeType.PNG
	}

	def "import ferman pcl file document 2"() {
		when:
		handlerChain.importFile(document: document, documentData: pclDocumentData, bytes: pclBytes,
				pclDocument: pclInfo.documents[1])

		then:
		// Just to make sure the business service was called
		document.searchField("DocumentType")
		document.files.first().pages == 2
		document.files.first().mimeType == MimeType.PDF
		document.files.last().mimeType == MimeType.PCL
		document.files.first().fileKey != document.files.last().fileKey
		fileService.getBytes(document.files.first())[0..3] == "%PDF".bytes
		document.previewImages.size() == 2
		document.previewImage(1).data.pages == 1
		document.previewImage(1).data.mimeType == MimeType.PNG
	}
}
