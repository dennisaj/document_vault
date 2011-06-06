package us.paperlesstech.handlers

import org.springframework.core.io.ClassPathResource
import us.paperlesstech.Document
import us.paperlesstech.DocumentData
import us.paperlesstech.DomainIntegrationSpec
import us.paperlesstech.MimeType

class DefaultImageHandlerServiceIntegrationSpec extends BaseHandlerSpec {
	def defaultImageHandlerService
	def line

	@Override
	def setup() {
		line = [a:[x:0,y:0], b:[x:100,y:100]]
		defaultImageHandlerService.authService = authService
	}

	def "import image files"() {
		when:
			def document = new Document(group: DomainIntegrationSpec.group)
			def documentData = new DocumentData(data: new ClassPathResource("test" + mimeType.downloadExtension).getFile().getBytes(), mimeType: mimeType)
			def input = [document: document, documentData: documentData]
			defaultImageHandlerService.importFile(input)
		then:
			document.files.first().pages == 1
			document.files.first().mimeType == mimeType
			document.previewImages.size() == 1
			document.previewImage(1).data.id == document.files.first().id
			document.previewImage(1).data.pages == 1
			document.previewImage(1).data.mimeType == mimeType
		where:
			mimeType << [MimeType.PNG, MimeType.JPEG, MimeType.BMP, MimeType.GIF]
	}

	def "sign image files"() {
		given:
			def lines = ['1':[line, 'LB'], '2':[line], '4':[line]]
		when:
			def document = new Document(group: DomainIntegrationSpec.group)
			def documentData = new DocumentData(data: new ClassPathResource("test" + mimeType.downloadExtension).getFile().getBytes(), mimeType: mimeType)
			def input = [document: document, documentData: documentData]

			defaultImageHandlerService.importFile(input)
			document = document.save()
			input = [document: document, documentData: document.files.first(), signatures: lines]
			defaultImageHandlerService.sign(input)

		then:
			document.files.size() == 2
			document.files.first().pages == 1
			document.files.first().mimeType == mimeType
			document.files.first().data != document.files.last().data
			document.previewImages.size() == 1
			document.previewImages*.data.pages == [1] * 1
			document.previewImages*.data.mimeType == [mimeType] * 1
			document.previewImages*.pageNumber == [1]
			document.signed == true
		where:
			mimeType << [MimeType.PNG, MimeType.JPEG, MimeType.BMP, MimeType.GIF]
	}
}
