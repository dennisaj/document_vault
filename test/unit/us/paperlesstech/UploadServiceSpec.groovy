package us.paperlesstech

import grails.plugin.spock.UnitSpec
import us.paperlesstech.handlers.HandlerChain
import us.paperlesstech.nimble.Group

class UploadServiceSpec extends UnitSpec {
	UploadService service
	AuthService authService = Mock()
	FileService fileService = Mock()
	HandlerChain handlerChain = Mock()

	def setup() {
		mockLogging(UploadService)
		service = new UploadService()
		service.authService = authService
		service.fileService = fileService
		service.handlerChain = handlerChain
	}

	def "uploadInputStream only works if it has a mimeType"() {
		expect:
		service.uploadInputStream(null, new Group(), "file with no ext", "unknown content type") == null
	}

	def "uploadInputStream should pass through mimeType"() {
		given:
		def d = new Document()
		service.metaClass.uploadDocument = { byte[] bytes, Group group, String name, MimeType mimeType -> [d] }

		when:
		def result = service.uploadInputStream(null as InputStream, new Group(), "file.pdf", "application/pdf")

		then:
		result == [d]
	}

	def "uploadDocument verifies the user can upload"() {
		given:
		def group = new Group()

		when:
		service.uploadDocument(null as byte[], group, "file.pdf", MimeType.PDF)

		then:
		thrown AssertionError
		1 * authService.canUpload(group) >> false
	}

	def "uploadDocumentData doesn't return the document on error"() {
		given:
		def group = new Group()
		def bytes = new byte[1]

		when:
		service.uploadDocument(bytes, group, "file.pdf", MimeType.PDF) == null

		then:
		1 * authService.canUpload(group) >> true
		1 * handlerChain.importFile(_) >> { throw new RuntimeException("Error") }
	}

	def "uploadDocumentData imports and saves the passed document"() {
		given:
		mockDomain(Document)
		def group = new Group()
		def bytes = new byte[1]
		def capturedDoc

		when:
		def returnedDoc = service.uploadDocument(bytes, group, "file.pdf", MimeType.PDF)

		then:
		1 * authService.canUpload(group) >> true
		// Capture the passed file and mock out save and files on it
		1 * handlerChain.importFile({ capturedDoc = it.document }) >> {
			capturedDoc.metaClass.save = { capturedDoc }
			capturedDoc.addToFiles(new DocumentData())
		}
		returnedDoc == [capturedDoc]
	}
}
