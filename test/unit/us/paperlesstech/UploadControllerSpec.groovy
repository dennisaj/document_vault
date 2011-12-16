package us.paperlesstech

import org.springframework.core.io.ClassPathResource

import us.paperlesstech.nimble.Group
import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import spock.lang.Specification
import org.codehaus.groovy.grails.plugins.testing.GrailsMockMultipartFile

@TestFor(UploadController)
@Mock([Folder, Document, DocumentData, PreviewImage, Group])
class UploadControllerSpec extends Specification {
	AuthService authService = Mock()
	UploadService uploadService = Mock()
	GrailsMockMultipartFile testFile = null

	def setup() {
		controller.authService = authService
		controller.uploadService = uploadService

		Document.authService = authService
		Document.metaClass.getFlags = {-> [] }

		def source = new ClassPathResource("dt_cust_hard.pcl").file
		testFile = new GrailsMockMultipartFile(source.name, source.bytes)
	}

	def cleanup() {
		Document.metaClass.getFlags = null
	}

	// TODO uncomment these when Grails 2.0.1 comes out and see if they work
//	def "savePcl responds with a 500 error when the user can't upload to any group"() {
//		when:
//		controller.savePcl()
//
//		then:
//		1 * authService.getGroupsWithPermission([DocumentPermission.Upload]) >> null
//		response.status == 500
//	}
//
//	def "savePcl responds with a 500 error when it can't save"() {
//		when:
//		controller.savePcl()
//
//		then:
//		authService.getGroupsWithPermission(DocumentPermission.Upload) >> ([new Group()] as SortedSet)
//		uploadService.uploadByteArray(_, _, _, _) >> [null, { throw new RuntimeException("boom") }]
//		response.status == 500
//	}
//
//	def "savePcl responds with 200 when it saves"() {
//		given:
//		def doc = new Document()
//		doc.id = 42
//
//		when:
//		controller.savePcl()
//
//		then:
//		1 * authService.getGroupsWithPermission([DocumentPermission.Upload]) >> ([new Group()] as SortedSet)
//		1 * uploadService.uploadDocument(_, _, _, _) >> [doc]
//		response.status == 200
//		response.text.contains("42")
//	}
//
//	def "save with no group returns an error"() {
//		given:
//		messageSource.addMessage('document-vault.upload.error.missinggroup', request.locale, 'missing')
//		controller.params.group = "-1"
//
//		when:
//		controller.save()
//		def json = response.json
//
//		then:
//		json.size() == 1
//		json[0].error == "missing"
//	}
//
//	def "save returns an error when a document isn't created"() {
//		given:
//		messageSource.addMessage('document-vault.upload.error.unsupportedfile', request.locale, 'unsupported')
//		def group = new Group(name: 'group')
//		group.save(flush: true, failOnError: true)
//		controller.params.group = "${group.id}"
//		request.addFile(testFile)
//
//		when:
//		controller.save()
//		def json = response.json
//
//		then:
//		1 * uploadService.uploadInputStream(_, group, _, _, _) >> null
//		json.size() == 1
//		json[0].error == "unsupported"
//	}
//
//	def "a successful save returns document name, size, and url"() {
//		given:
//		def group = new Group(name: 'group')
//		group.save(flush: true, failOnError: true)
//		controller.params.group = "${group.id}"
//		controller.metaClass.createLink = { "mooLink" }
//		request.addFile(testFile)
//		def d = UnitTestHelper.createDocument()
//
//		when:
//		controller.save()
//		def json = response.json
//
//		then:
//		1 * uploadService.uploadInputStream(_, group, _, _, _) >> [d]
//		json.size() == 1
//		!json[0].error
//		json[0].name == d.name
//		json[0].data.size == d.files.first().fileSize
//		json[0].data.mimeType == d.files.first().mimeType.name().toLowerCase()
//	}
}
