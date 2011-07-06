package us.paperlesstech.handlers

import grails.plugin.spock.IntegrationSpec

import org.springframework.core.io.ClassPathResource

import us.paperlesstech.Document
import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType
import grails.plugins.nimble.core.Group
import us.paperlesstech.DomainIntegrationSpec

class PclHandlerServiceIntegrationSpec extends BaseHandlerSpec {
	def fileService
	def handlerChain
	def document
	def pclData

	def setup() {
		document = new Document()
		document.group = DomainIntegrationSpec.group
		pclData = fileService.createDocumentData(mimeType: MimeType.PCL, file: new ClassPathResource("dt_combined.pcl").getFile())
	}

	def "import ferman pcl file"() {
		def input = [document: document, documentData: pclData]
		when:
		handlerChain.importFile(input)

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
