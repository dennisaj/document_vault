package us.paperlesstech

import grails.plugin.spock.UnitSpec
import grails.plugins.nimble.core.Group
import us.paperlesstech.handlers.HandlerChain

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
		service.metaClass.uploadDocumentData = { DocumentData dd, Group group, String name, MimeType mimeType -> d }

		when:
		def result = service.uploadInputStream(null as InputStream, new Group(), "file.pdf", "application/pdf")

		then:
		1 * fileService.createDocumentData(_) >> new DocumentData()
		result == d
	}

	def "uploadByteArray should create a DocumentData to pass through"() {
		given:
		def d = new Document()
		def documentData = new DocumentData()
		service.metaClass.uploadDocumentData = { DocumentData dd, Group group, String name, MimeType mimeType ->
			if (dd == documentData) {
				return d
			}
		}

		when:
		def result = service.uploadByteArray(null as byte[], new Group(), "file.pdf", MimeType.PDF)

		then:
		1 * fileService.createDocumentData(_) >> documentData
		result == d
	}

	def "uploadDocumentData verifies the user can upload"() {
		given:
		def group = new Group()

		when:
		service.uploadDocumentData(null as DocumentData, group, "file.pdf", MimeType.PDF)

		then:
		thrown AssertionError
		1 * authService.canUpload(group) >> false
	}

	def "uploadDocumentData doesn't return the document on error"() {
		given:
		def group = new Group()
		def dd = new DocumentData()

		when:
		service.uploadDocumentData(dd, group, "file.pdf", MimeType.PDF) == null

		then:
		1 * authService.canUpload(group) >> true
		1 * handlerChain.importFile(_) >> { throw new RuntimeException("Error") }
	}

	def "uploadDocumentData imports and saves the passed document"() {
		given:
		mockDomain(Document)
		def group = new Group()
		def dd = new DocumentData()
		def capturedDoc

		when:
		def returnedDoc = service.uploadDocumentData(dd, group, "file.pdf", MimeType.PDF)

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
