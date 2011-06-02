package us.paperlesstech.handlers

import grails.plugin.spock.IntegrationSpec

import org.springframework.core.io.ClassPathResource

import us.paperlesstech.Document
import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType
import grails.plugins.nimble.core.Group
import us.paperlesstech.DomainIntegrationSpec

class PclHandlerServiceIntegrationSpec extends BaseHandlerSpec {
	def handlerChain
	def document
	def pclData

	def setup() {
		document = new Document()
		document.group = DomainIntegrationSpec.group
		pclData = new DocumentData(mimeType: MimeType.PCL)
		pclData.data = new ClassPathResource("dt_combined.pcl").getFile().getBytes()
	}

	def "import ferman pcl file"() {
		def input = [document: document, documentData: pclData]
		when:
		handlerChain.importFile(input)

		then:
		document.searchField("DocumentType") == "CustomerHardCopy"
		document.files.first().pages == 1
		document.files.first().mimeType == MimeType.PDF
		document.files.first().data[0..3] == "%PDF".bytes
		document.previewImages.size() == 1
		document.previewImage(1).data.pages == 1
		document.previewImage(1).data.mimeType == MimeType.PNG
	}
}
