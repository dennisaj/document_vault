package us.paperlesstech

import grails.plugin.spock.UnitSpec
import grails.plugins.nimble.core.Group
import us.paperlesstech.handlers.HandlerChain

class UploadServiceSpec extends UnitSpec {
	UploadService service
	AuthService authService = Mock()
	HandlerChain handlerChain = Mock()

	def setup() {
		mockLogging(UploadService)
		service = new UploadService()
		service.authService = authService
		service.handlerChain = handlerChain
	}

	def "upload only works if it has a mimeType"() {
		expect:
		service.upload(new Group(), "file with no ext", null, "unknown content type") == null
	}

	def "upload that finds the mimeType should pass it through"() {
		given:
		Document d = new Document()
		service.metaClass.upload = { Group group, String name, byte[] data, MimeType mimeType -> d }

		expect:
		service.upload(new Group(), "file.pdf", null, "application/pdf") == d
	}

	def "upload verifies the user can upload"() {
		given:
		def group = new Group()

		when:
		service.upload(group, "file.pdf", null, MimeType.PDF)

		then:
		thrown AssertionError
		1 * authService.canUpload(group) >> false
	}

	def "upload doesn't return the document on error"() {
		given:
		def group = new Group()

		when:
		service.upload(group, "file.pdf", null, MimeType.PDF) == null

		then:
		1 * authService.canUpload(group) >> true
		1 * handlerChain.importFile(_) >> { throw new RuntimeException("Error") }
	}

	def "upload imports and saves the passed document"() {
		given:
		mockDomain(Document)
		def group = new Group()
		def capturedDoc

		when:
		def returnedDoc = service.upload(group, "file.pdf", null, MimeType.PDF)

		then:
		1 * authService.canUpload(group) >> true
		// Capture the passed file and mock out save and files on it
		1 * handlerChain.importFile({ capturedDoc = it.document }) >> {
			capturedDoc.metaClass.save = { capturedDoc }
			capturedDoc.addToFiles(new DocumentData())
		}
		returnedDoc == capturedDoc
	}
}
