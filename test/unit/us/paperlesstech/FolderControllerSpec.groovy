package us.paperlesstech

import grails.converters.JSON
import grails.plugin.spock.ControllerSpec
import spock.lang.Shared
import us.paperlesstech.helpers.NotificationStatus
import us.paperlesstech.nimble.Group

class FolderControllerSpec extends ControllerSpec {
	FolderService folderService = Mock()

	def group1 = new Group(id:1, name:'group1')
	def group2 = new Group(id:2, name:'group2')
	def folder1 = new Folder(id:1, name:'folder1')
	def folder2 = new Folder(id:2, name:'folder2')
	def folder3 = new Folder(id:3, name:'folder3')
	def document1 = new Document(id:1, name:'document1')
	def document2 = new Document(id:2, name:'document2')
	def document3 = new Document(id:3, name:'document3')
	@Shared
	def bucket1 = new Bucket(id:1, name:'bucket1')

	def setup() {
		controller.folderService = folderService
		controller.metaClass.message = { LinkedHashMap arg1 -> 'this is stupid' }

		mockDomain(Group, [group1, group2])
		mockDomain(Bucket, [bucket1])
		mockDomain(Folder, [folder1, folder2, folder3])
		mockDomain(Document, [document1, document2, document3])

		folder1.group = group1
		folder2.group = group1
		folder3.group = group2

		bucket1.group = group1

		document1.group = group1
		document2.group = group1
		document3.group = group1

		document2.folder = folder1
		folder1.addToDocuments(document2)
	}

	def "create should throw an AssertError when given invalid input"() {
		given:
		controller.params.groupId = groupId
		controller.params.documentId = documentId
		when:
		controller.create()
		then:
		0 * folderService.createFolder(_, _, _)
		thrown(AssertionError)
		where:
		groupId  | documentId
		'9'      | '1'      // Bad groupId
		null     | '1'      // Bad groupId
		'1'      | '4'      // Bad documentId
		'1'      | null     // Bad documentId
	}

	def "create should render errors return by createFolder"() {
		given:
		controller.params.groupId = '1'
		controller.params.documentId = '1'
		controller.params.name = '   new folder   '
		when:
		controller.create()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		1 * folderService.createFolder(group1, 'new folder', document1) >> { group, name, document->
			def folder = new Folder(group:group, name:name)
			folder.addToDocuments(document)
			folder.errors.rejectValue('name', 'this.is.an.error.code.for.name')
			folder
		}
		results.notification.status == NotificationStatus.Error.name()
		results.validation.name.errors
		!results.validation.name.valid
		!results.validation.group.errors
		results.validation.group.valid
	}

	def "create should render the saved folder when there are no errors returned by createFolder"() {
		controller.params.groupId = '1'
		controller.params.documentId = '1'
		controller.params.name = '   new folder2   '
		when:
		controller.create()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		1 * folderService.createFolder(group1, 'new folder2', document1) >> { group, name, document->
			def folder = new Folder(id:4, group:group, name:name)
			folder.addToDocuments(document)
			folder
		}
		results.notification.status == NotificationStatus.Success.name()
		results.folder.id == 4
		results.folder.name == 'new folder2'
		results.folder.group.id == group1.id
		results.folder.group.name == group1.name
		results.folder.documents[0].id == document1.id
		results.folder.documents[0].name == document1.name
	}

	def "delete should throw an AssertError when given an invalid folder"() {
		given:
		controller.params.folderId = folderId
		when:
		controller.delete()
		then:
		0 * folderService.deleteFolder(_)
		thrown(AssertionError)
		where:
		folderId << [null, '123']
	}

	def "delete should call deleteFolder and return a JSON object with a notification entry"() {
		given:
		controller.params.folderId = '1'
		when:
		controller.delete()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		1 * folderService.deleteFolder(folder1)
		results.notification.status == NotificationStatus.Success.name()
	}

	def "list should pass bucket, pagination and filter to search then return the results as JSON"() {
		given:
		controller.params.bucketId = bucketId
		controller.params.filter = filter
		controller.params.putAll(pagination)
		when:
		controller.list()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		1 * folderService.search(bucket, _, filter) >> [results:[folder1, folder2], total:2]
		results.folders[0].id == folder1.id
		results.folders[0].name == folder1.name
		results.folders[1].id == folder2.id
		results.folders[1].name == folder2.name
		results.total == 2
		where:
		filter << [null, 'filter']
		bucketId << [null, '1']
		bucket << [null, bucket1]
		pagination << [[:], [sort:'name', order:'asc', max:'10', offset:'0']]
	}

	def "addDocument should throw an AssertionError when passed invalid data"() {
		given:
		controller.params.folderId = folderId
		controller.params.documentId = documentId
		controller.params.currentFolderId = currentFolderId
		when:
		controller.addDocument()
		then:
		0 * folderService.addDocumentToFolder(_, _)
		thrown(AssertionError)
		where:
		folderId | documentId | currentFolderId
		'9'      | '2'        | '1'             // Bad folderId
		null     | '2'        | '1'             // Bad folderId
		'2'      | '9'        | '1'             // Bad documentId
		'2'      | null       | '1'             // Bad documentId
		'2'      | '2'        | '2'             // Incorrect currentFolderId
		'2'      | '2'        | null            // Incorrect currentFolderId
	}

	def "addDocument should addDocumentToFolder when given valid data"() {
		given:
		controller.params.folderId = '2'
		controller.params.documentId = '2'
		controller.params.currentFolderId = '1'
		when:
		controller.addDocument()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		1 * folderService.addDocumentToFolder(folder2, document2)
		results.notification.status == NotificationStatus.Success.name()
	}

	def "removeDocument should throw an AssertionError when passed invalid data"() {
		given:
		controller.params.folderId = folderId
		controller.params.documentId = documentId
		when:
		controller.removeDocument()
		then:
		0 * folderService.removeDocumentFromFolder(_)
		thrown(AssertionError)
		where:
		folderId | documentId
		'9'      | '2'      // Bad folderId
		null     | '2'      // Bad folderId
		'2'      | '9'      // Bad documentId
		'2'      | null     // Bad documentId
		'2'      | '2'      // Bad current folder
	}

	def "removeDocument should addFolderToBucket when given valid data"() {
		given:
		controller.params.folderId = '1'
		controller.params.documentId = '2'
		when:
		controller.removeDocument()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		1 * folderService.removeDocumentFromFolder(document2)
		results.notification.status == NotificationStatus.Success.name()
	}
}
