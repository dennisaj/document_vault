package us.paperlesstech.handlers

import org.springframework.core.io.ClassPathResource
import us.paperlesstech.Document
import us.paperlesstech.DomainIntegrationSpec
import us.paperlesstech.MimeType
import us.paperlesstech.DocumentData

class DefaultImageHandlerServiceIntegrationSpec extends BaseHandlerSpec {
	def defaultImageHandlerService
	def fileService
	def signature
	def line = [a:[x:0,y:0], b:[x:100,y:100]]

	@Override
	def setup() {
		signature = [height: 150, width: 750, top: 0, left: 0, lines:[line, 'LB']]
		defaultImageHandlerService.authService = authService
	}

	def "imagethumbnail should be set"() {
		expect:
			DefaultImageHandlerService.imagethumbnail
	}

	def "import image files"() {
		when:
			def document = new Document(group: DomainIntegrationSpec.group)
			def documentData = new DocumentData(mimeType: mimeType)
			def bytes = new ClassPathResource("test" + mimeType.downloadExtension).getFile().bytes
			def input = [document: document, documentData: documentData, bytes: bytes]
			defaultImageHandlerService.importFile(input)
		then:
			document.files.first().pages == 1
			document.files.first().mimeType == mimeType
			document.previewImages.size() == 1
			document.previewImage(1).data.id == document.files.first().id
			document.previewImage(1).data.pages == 1
			document.previewImage(1).data.mimeType == mimeType
			document.previewImage(1).thumbnail.pages == 1
			document.previewImage(1).thumbnail.mimeType == MimeType.PNG
		where:
			mimeType << [MimeType.PNG, MimeType.JPEG, MimeType.BMP, MimeType.GIF]
	}

	def "cursiveSign image files"() {
		when:
			def document = new Document(group: DomainIntegrationSpec.group)
			def documentData = new DocumentData(mimeType: mimeType)
			def bytes = new ClassPathResource("test" + mimeType.downloadExtension).getFile().bytes
			def input = [document: document, documentData: documentData, bytes: bytes]

			defaultImageHandlerService.importFile(input)
			document = document.save()
			input = [document: document, documentData: document.files.first(), signatures: ["1":[signature]]]
			defaultImageHandlerService.cursiveSign(input)

		then:
			document.files.size() == 2
			document.files.first().pages == 1
			document.files.first().mimeType == mimeType
			document.files.first().fileKey != document.files.last().fileKey
			document.previewImages.size() == 1
			document.previewImages*.data.pages == [1] * 1
			document.previewImages*.data.mimeType == [mimeType] * 1
			document.previewImages*.thumbnail.pages == [1] * 1
			document.previewImages*.thumbnail.mimeType == [MimeType.PNG] * 1
			document.previewImages*.pageNumber == [1]
		where:
			mimeType << [MimeType.PNG, MimeType.JPEG, MimeType.BMP, MimeType.GIF]
	}
}
