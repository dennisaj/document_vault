package us.paperlesstech

import grails.converters.JSON
import grails.plugin.spock.ControllerSpec
import org.springframework.core.io.ClassPathResource

import us.paperlesstech.nimble.Group

class UploadControllerSpec extends ControllerSpec {
	AuthService authService = Mock()
	UploadService uploadService = Mock()
	PreferenceService preferenceService = Mock()
	File testFile = new ClassPathResource("dt_cust_hard.pcl").file

	def setup() {
		testFile.metaClass.getOriginalFilename = { "originalName.pcl" }
		testFile.metaClass.getContentType = { "application/pcl" }
		testFile.metaClass.getInputStream = { Mock(InputStream) }
		controller.authService = authService
		controller.uploadService = uploadService
		controller.preferenceService = preferenceService
	}

	def "savePcl responds with a 500 error when the user can't upload to any group"() {
		when:
		controller.savePcl()

		then:
		1 * authService.getGroupsWithPermission([DocumentPermission.Upload]) >> null
		mockResponse.status == 500
	}

	def "savePcl responds with a 500 error when it can't save"() {
		when:
		controller.savePcl()

		then:
		authService.getGroupsWithPermission(DocumentPermission.Upload) >> ([new Group()] as SortedSet)
		uploadService.uploadByteArray(_, _, _, _) >> [null, { throw new RuntimeException("boom") }]
		mockResponse.status == 500
	}

	def "savePcl responds with 200 when it saves"() {
		given:
		def doc = new Document()
		doc.id = 42

		when:
		controller.savePcl()

		then:
		1 * authService.getGroupsWithPermission([DocumentPermission.Upload]) >> ([new Group()] as SortedSet)
		1 * uploadService.uploadDocument(_, _, _, _) >> [doc]
		mockResponse.status == 200
		mockResponse.contentAsString.contains("42")
	}

	def "index should return the groups that the user can upload to"() {
		when:
		1 * preferenceService.getPreference(_, _) >> defaultGroup
		1 * authService.getGroupsWithPermission([DocumentPermission.Upload]) >> groups
		def model = controller.index()

		then:
		model == [groups: groups, defaultGroup:defaultGroup]

		where:
		groups = ["moo"] as SortedSet
		defaultGroup << ["group"]
	}

	def "ajaxSave should call the real save with ajax set to true"() {
		given:
		def params
		controller.metaClass.save = { params = it }

		when:
		controller.ajaxSave()

		then:
		params.ajax == true
	}

	def "save with no group returns an error"() {
		given:
		def group = new Group(id: 1)
		mockDomain(Group, [group])
		controller.params.group = "-1"
		controller.metaClass.message = { Map m -> return "moo" }
		controller.params.ajax = ajax
		def resultMap = [:]

		when:
		controller.save()

		then:
		if (ajax) {
			def json = JSON.parse(mockResponse.contentAsString)
			assert json.size() == 1
			resultMap = json[0]
		} else {
			assert chainArgs.action == "index"
			assert chainArgs.model.results.size() == 1
			resultMap = chainArgs.model.results[0]
		}
		resultMap.error == "moo"

		where:
		ajax << [true, false]
	}

	def "save returns an error when a document isn't created"() {
		given:
		def group = new Group(id: 1)
		mockDomain(Group, [group])
		controller.params.group = "1"
		controller.metaClass.message = { Map m -> return "moo" }
		controller.params.ajax = ajax
		mockRequest.metaClass.getMultiFileMap = { ["testFiles": [testFile]]}
		def resultMap = [:]

		when:
		controller.save()

		then:
		1 * uploadService.uploadInputStream(_, group, _, _) >> null
		if (ajax) {
			def json = JSON.parse(mockResponse.contentAsString)
			assert json.size() == 1
			resultMap = json[0]
		} else {
			assert chainArgs.action == "index"
			assert chainArgs.model.results.size() == 1
			resultMap = chainArgs.model.results[0]
		}
		resultMap.error == "moo"

		where:
		ajax << [true, false]
	}

	def "a successful save returns document name, size, and url"() {
		given:
		def group = new Group(id: 1)
		mockDomain(Group, [group])
		controller.params.group = "1"
		controller.params.ajax = ajax
		controller.metaClass.createLink = { "mooLink" }
		mockRequest.metaClass.getMultiFileMap = { ["testFiles": [testFile]]}
		def dd = new DocumentData(fileSize: fileSize)
		mockDomain(Document)
		def d = new Document(name: "docName")
		d.metaClass.addTags = {tags-> }
		d.addToFiles(dd)
		d.addToPreviewImages(new PreviewImage(data:dd, pageNumber:1, thumbnail:dd))
		def resultMap = [:]

		when:
		controller.save()

		then:
		1 * uploadService.uploadInputStream(_, group, _, _) >> [d]
		if (ajax) {
			def json = JSON.parse(mockResponse.contentAsString)
			assert json.size() == 1
			resultMap = json[0]
		} else {
			assert chainArgs.action == "index"
			assert chainArgs.model.results.size() == 1
			resultMap = chainArgs.model.results[0]
		}
		!resultMap.error
		resultMap.name == "docName"
		resultMap.size == fileSize
		resultMap.url == "mooLink"

		where:
		ajax << [true, false]
		fileSize << [42, 24]
	}
}
