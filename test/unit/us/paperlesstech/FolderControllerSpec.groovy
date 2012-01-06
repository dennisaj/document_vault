package us.paperlesstech

import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import spock.lang.Specification

import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.User

@TestFor(FolderController)
@Mock([Group, Document, DocumentData, PreviewImage, Folder, User])
class FolderControllerSpec extends Specification {
	AuthService authService = Mock()
	DocumentService documentService = Mock()
	FolderService folderService = Mock()
	NotificationService notificationService = Mock()
	TenantService tenantService = Mock()

	def group1
	def group2
	def folder1
	def folder2
	def folder3
	def document1
	def document2
	def document3
	def parent1
	def parent2

	def setup() {
		controller.authService = authService
		controller.documentService = documentService
		controller.folderService = folderService
		controller.notificationService = notificationService
		controller.tenantService = tenantService

		Document.metaClass.getTags = {-> null }
		Folder.metaClass.getTags = {-> null }

		group1 = new Group(id:1, name:'group1').save(failOnError:true)
		group2 = new Group(id:2, name:'group2').save(failOnError:true)
		folder1 = new Folder(id:1, name:'folder1', group:group1).save(failOnError:true)
		folder2 = new Folder(id:2, name:'folder2', group:group1).save(failOnError:true)
		folder3 = new Folder(id:3, name:'folder3', group:group2).save(failOnError:true)
		document1 = UnitTestHelper.createDocument(group:group1)
		document2 = UnitTestHelper.createDocument(group:group1)
		document3 = UnitTestHelper.createDocument(group:group1)
		parent1 = new Folder(id:4, group:group1, name:'parent1').save(failOnError:true)
		parent2 = new Folder(id:5, group:group2, name:'parent2').save(failOnError:true)

		document2.folder = folder1
		folder1.addToDocuments(document2)

		folder1.parent = parent1
		parent1.addToChildren(folder1)

		document1.save(failOnError:true)
		document2.save(failOnError:true)
		document3.save(failOnError:true)

		Folder.authService = authService
		Document.authService = authService
	}

	def cleanup() {
		Document.metaClass.getTags = null
		Folder.metaClass.getTags = null
	}

	def "create should throw an AssertError when given invalid input"() {
		when:
		controller.create(groupId, 'name', null)
		0 * folderService.createFolder(_, _, _)
		then:
		thrown(AssertionError)
		where:
		groupId  << [9L, null]
	}

	def "create should render errors return by createFolder"() {
		when:
		controller.create(1L, '   new folder   ', null)
		def results = JSON.parse(response.contentAsString)
		then:
		1 * folderService.createFolder(group1, 'new folder', null) >> { group, name, parent ->
			def folder = new Folder(group:group, name:name)
			folder.parent = parent
			folder.errors.rejectValue('name', 'this.is.an.error.code.for.name')
			folder
		}
		1 * notificationService.error(_)
		results.validation.name.errors
		!results.validation.name.valid
		!results.validation.group.errors
		results.validation.group.valid
	}

	def "create should render the saved folder when there are no errors returned by createFolder"() {
		when:
		controller.create(1L, '   new folder2   ', null)
		def results = JSON.parse(response.contentAsString)
		then:
		1 * folderService.createFolder(group1, 'new folder2', null) >> { group, name, parent ->
			def folder = new Folder(id:6, group:group, name:name).save(failOnError:true)
			folder
		}
		1 * notificationService.success(_, _)
		results.folder.id == 6
		results.folder.name == 'new folder2'
		results.folder.group.id == group1.id
		results.folder.group.name == group1.name
	}

	def "delete should throw an AssertError when given an invalid folder"() {
		when:
		controller.delete(folderId)
		0 * folderService.deleteFolder(_)
		then:
		thrown(AssertionError)
		where:
		folderId << [null, 123L]
	}

	def "delete should call deleteFolder and return a JSON object with a notification entry"() {
		when:
		controller.delete(1L)
		def results = JSON.parse(response.contentAsString)
		then:
		1 * folderService.deleteFolder(folder1)
		1 * notificationService.success(_, _)
	}

	def "list should pass parent, pagination and filter to folderService filter then return the results as JSON"() {
		when:
		controller.list(folderId, filter, pagination.max, pagination.sort, pagination.order, pagination.offset)
		def results = JSON.parse(response.contentAsString)
		then:
		1 * folderService.filter(Folder.get(folderId), _, filter) >> [results:[folder1, folder2], total:2]
		results.folders[0].id == folder1.id
		results.folders[0].name == folder1.name
		results.folders[1].id == folder2.id
		results.folders[1].name == folder2.name
		results.folderTotal == 2
		where:
		filter << [null, 'filter']
		folderId << [null, 4L]
		pagination << [[:], [sort:'name', order:'asc', max:10, offset:0]]
	}

	def "addDocument should throw an AssertionError when passed invalid data"() {
		when:
		controller.addDocument(folderId, documentId, currentFolderId)
		0 * folderService.addDocumentToFolder(_, _)
		then:
		thrown(AssertionError)
		where:
		folderId | documentId | currentFolderId
		9L       | 2L         | 1L             // Bad folderId
		null     | 2L         | 1L             // Bad folderId
		2L       | 9L         | 1L             // Bad documentId
		2L       | null       | 1L             // Bad documentId
		2L       | 2L         | 2L             // Incorrect currentFolderId
		2L       | 2L         | null           // Incorrect currentFolderId
	}

	def "addDocument should addDocumentToFolder when given valid data"() {
		when:
		controller.addDocument(2L, 2L, 1L)
		def results = JSON.parse(response.contentAsString)
		then:
		1 * folderService.addDocumentToFolder(folder2, document2)
		1 * notificationService.success(_, _)
	}

	def "removeDocument should throw an AssertionError when passed invalid data"() {
		when:
		controller.removeDocument(folderId, documentId)
		0 * folderService.removeDocumentFromFolder(_)
		then:
		thrown(AssertionError)
		where:
		folderId | documentId
		9L       | 2L      // Bad folderId
		null     | 2L      // Bad folderId
		2L       | 9L      // Bad documentId
		2L       | null    // Bad documentId
		2L       | 2L      // Bad current folder
	}

	def "removeDocument should removeDocumentFromFolder when given valid data"() {
		when:
		controller.removeDocument(1L, 2L)
		def results = JSON.parse(response.contentAsString)
		then:
		1 * folderService.removeDocumentFromFolder(document2)
		1 * notificationService.success(_, _)
	}

	def "addFolder should throw an AssertionError when passed invalid data"() {
		when:
		controller.addFolder(parentId, childId, currentParentId)
		0 * folderService.addChildToFolder(_, _)
		then:
		thrown(AssertionError)
		where:
		parentId | childId | currentParentId
		9L       | 1L      | 4L             // Bad parentId
		null     | 1L      | 4L             // Bad parentId
		5L       | 9L      | 4L             // Bad childId
		5L       | null    | 4L             // Bad childId
		5L       | 1L      | 5L             // Incorrect currentParentId
		5L       | 1L      | null           // Incorrect currentParentId
	}

	def "addFolder should addDocumentToFolder when given valid data"() {
		when:
		controller.addFolder(5L, 1L, 4L)
		def results = JSON.parse(response.contentAsString)
		then:
		1 * folderService.addChildToFolder(parent2, folder1)
		1 * notificationService.success(_, _)
	}

	def "update should throw an AssertError when given invalid input"() {
		when:
		controller.update(folderId, 'name')
		0 * folderService.renameFolder(_, _)
		then:
		thrown(AssertionError)
		where:
		folderId  << [9L, null]
	}

	def "update should render errors return by createFolder"() {
		when:
		controller.update(1L, '')
		def results = JSON.parse(response.contentAsString)
		then:
		1 * folderService.renameFolder(folder1, '') >> { folder, name ->
			folder.name = name
			folder.errors.rejectValue('name', 'this.is.an.error.code.for.name')
			folder
		}
		1 * notificationService.error(_)
		results.validation.name.errors
		!results.validation.name.valid
		!results.validation.group.errors
		results.validation.group.valid
	}

	def "update should render the saved folder when there are no errors returned by createFolder"() {
		when:
		controller.update(1L, '   new folder2   ')
		def results = JSON.parse(response.contentAsString)
		then:
		1 * folderService.renameFolder(folder1, 'new folder2') >> { folder, name->
			folder.name = name
			folder
		}
		1 * notificationService.success(_, _)
		results.folder.id == 1
		results.folder.name == 'new folder2'
	}

	def "removeFolder should throw an AssertionError when passed invalid data"() {
		given:
		controller.params.folderId = folderId
		controller.params.parentId = parentId
		when:
		controller.removeFolder(folderId, parentId)
		0 * folderService.removeChildFromFolder(_)
		then:
		thrown(AssertionError)
		where:
		folderId | parentId
		9L       | 2L      // Bad folderId
		null     | 2L      // Bad folderId
		2L       | 9L      // Bad parentId
		2L       | null    // Bad parentId
		2L       | 2L      // Bad current folder
	}

	def "removeFolder should removeDocumentFromFolder when given valid data"() {
		when:
		controller.removeFolder(folder1.parent.id, folder1.id)
		def results = JSON.parse(response.contentAsString)
		then:
		1 * folderService.removeChildFromFolder(folder1)
		1 * notificationService.success(_, _)
	}

	def 'pin should use the folderService to pin the folder'() {
		when:
		controller.pin(folder1.id)
		def results = JSON.parse(response.contentAsString)
		then:
		1 * folderService.pin(folder1)
		1 * notificationService.success(_, _)
	}

	def 'unpin should use the folderService to unpin the folder'() {
		when:
		controller.unpin(folder1.id)
		def results = JSON.parse(response.contentAsString)
		then:
		1 * folderService.unpin(folder1)
		1 * notificationService.success(_, _)
	}

	def "show should call the default folder_list document_list and folder_ancestry"() {
		when:
		controller.show(parent1.id)
		def results = JSON.parse(response.contentAsString)
		then:
		1 * folderService.ancestry(parent1) >> [parent2]
		1 * folderService.filter(parent1, _, null) >> [results:[folder1, folder2], total:2]
		1 * documentService.filter(parent1, _, null) >> [results:[document2, document3], total:2]
		results.folderTotal == 2
		results.folders[0].id == folder1.id
		results.folders[0].name == folder1.name
		results.folders[1].id == folder2.id
		results.folders[1].name == folder2.name
		results.documentTotal == 2
		results.documents[0].id == document2.id
		results.documents[0].name == document2.name
		results.documents[1].id == document3.id
		results.documents[1].name == document3.name
		results.searchFolder.ancestry[0].id == parent2.id
		results.searchFolder.ancestry[0].name == parent2.name
	}

	def 'flag requires the folderId'() {
		when:
		controller.flag(null, 'Flag')
		then:
		thrown(AssertionError)
	}

	def 'flag requires the flag name'() {
		when:
		controller.flag(folder1.id, null)
		then:
		thrown(AssertionError)
	}

	def 'flag requires the flag is a tenant flag'() {
		when:
		controller.flag(folder1.id, 'notaflag')
		then:
		1 * tenantService.getTenantConfigList('flag') >> ['Waiting']
		thrown(AssertionError)
	}

	def 'flag requires the user can manager folders'() {
		when:
		controller.flag(folder1.id, 'Waiting')
		then:
		1 * tenantService.getTenantConfigList('flag') >> ['Waiting']
		authService.canManageFolders(folder1.group) >> false
		thrown(AssertionError)
	}

	def 'flag returns success after tagging the folder'() {
		given:
		def tagAdded = null
		folder1.metaClass.addTag = { String tag -> tagAdded = tag }
		when:
		controller.flag(folder1.id, 'Waiting')
		def results = JSON.parse(response.contentAsString)
		then:
		tagAdded == 'Waiting'
		1 * tenantService.getTenantConfigList('flag') >> ['Waiting']
		authService.canManageFolders(folder1.group) >> true
		1 * notificationService.success(_, _)
	}

	def 'unflag requires the folderId'() {
		when:
		controller.unflag(null, 'Flag')
		then:
		thrown(AssertionError)
	}

	def 'unflag requires the flag name'() {
		when:
		controller.unflag(folder1.id, null)
		then:
		thrown(AssertionError)
	}

	def 'unflag requires the flag is a tenant flag'() {
		when:
		controller.unflag(folder1.id, 'notaflag')
		then:
		1 * tenantService.getTenantConfigList('flag') >> ['Waiting']
		thrown(AssertionError)
	}

	def 'unflag requires the user can manager folders'() {
		when:
		controller.unflag(folder1.id, 'Waiting')
		then:
		1 * tenantService.getTenantConfigList('flag') >> ['Waiting']
		1 * authService.canManageFolders(folder1.group) >> false
		thrown(AssertionError)
	}

	def 'unflag returns success if the folder did not have the tag'() {
		given:
		folder1.metaClass.getTags = { -> [] }
		when:
		controller.unflag(folder1.id, 'Waiting')
		def results = JSON.parse(response.contentAsString)
		then:
		1 * tenantService.getTenantConfigList('flag') >> ['Waiting']
		2 * authService.canManageFolders(folder1.group) >> true
		1 * notificationService.success(_, _)
	}

	def 'unflag returns success after removing the tag from the folder'() {
		given:
		def tagRemoved = null
		folder1.metaClass.getTags = { -> ['Waiting'] }
		folder1.metaClass.removeTag = { String tag -> tagRemoved = tag }
		folder1.metaClass.getFlags = { -> [] }
		when:
		controller.unflag(folder1.id, 'Waiting')
		def results = JSON.parse(response.contentAsString)
		then:
		tagRemoved == 'Waiting'
		1 * tenantService.getTenantConfigList('flag') >> ['Waiting']
		2 * authService.canManageFolders(folder1.group) >> true
		1 * notificationService.success(_, _)
	}
}
