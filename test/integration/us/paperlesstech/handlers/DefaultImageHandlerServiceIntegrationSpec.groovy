package us.paperlesstech.handlers

import grails.plugin.spock.IntegrationSpec

import org.springframework.core.io.ClassPathResource

import spock.lang.Shared
import us.paperlesstech.Document
import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType

class DefaultImageHandlerServiceIntegrationSpec extends IntegrationSpec {
	def defaultImageHandlerService

	def "import image files"() {
		when:
			def document = new Document()
			def documentData = new DocumentData(data: new ClassPathResource("test" + mimeType.downloadExtension).getFile().getBytes(), mimeType: mimeType)
			def input = [document: document, documentData: documentData]
			defaultImageHandlerService.importFile(input)
		then:
			document.files.first().pages == 1
			document.files.first().mimeType == mimeType
			document.files.first().id == documentData.id
			document.previewImages.size() == 1
			document.previewImage(1).data.pages == 1
			document.previewImage(1).data.mimeType == mimeType
		where:
			mimeType << [MimeType.PNG, MimeType.JPEG, MimeType.BMP, MimeType.GIF]
	}
}
