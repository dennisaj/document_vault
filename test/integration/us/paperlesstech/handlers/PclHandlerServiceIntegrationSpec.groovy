package us.paperlesstech.handlers

import grails.plugin.spock.UnitSpec
import us.paperlesstech.handlers.business_logic.FermanBusinessLogicService
import grails.plugin.spock.IntegrationSpec
import us.paperlesstech.Document
import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType
import org.springframework.core.io.ClassPathResource

class PclHandlerServiceIntegrationSpec extends IntegrationSpec {
	def pclHandlerService
	def document
	def pclData

	def setup() {
		document = new Document()
		pclData = new DocumentData(mimeType: MimeType.PCL)
		pclData.data = new ClassPathResource("dt_combined.pcl").getFile().getBytes()
	}

    def "import ferman pcl file"() {
		def input = [document: document, documentData: pclData]
		when:
		pclHandlerService.importFile(input)

		then:
		document.searchFields.DocumentType == "CustomerHardCopy"
		document.files.first().pages == 1
		document.files.first().mimeType == MimeType.PDF
		document.files.first().data[0..3] == "%PDF".bytes
		document.previewImages.size() == 1
		document.previewImage(1).data.pages == 1
		document.previewImage(1).data.mimeType == MimeType.PNG
    }
}
