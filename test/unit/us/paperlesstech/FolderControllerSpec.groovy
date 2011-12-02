package us.paperlesstech

import grails.converters.JSON
import grails.plugin.spock.ControllerSpec
import spock.lang.Shared
import us.paperlesstech.nimble.Group

class FolderControllerSpec extends ControllerSpec {
	AuthService authService = Mock()
	DocumentService documentService = Mock()
	FolderService folderService = Mock()
	NotificationService notificationService = Mock()
	TenantService tenantService = Mock()

	def group1 = new Group(id:1, name:'group1')
	def group2 = new Group(id:2, name:'group2')
	def folder1 = new Folder(id:1, name:'folder1')
	def folder2 = new Folder(id:2, name:'folder2')
	def folder3 = new Folder(id:3, name:'folder3')
	def documentData = new DocumentData(id:1, pages:4, dateCreated:new Date(), mimeType:MimeType.PNG, fileSize:123)
	def previewImage = new PreviewImage(id:1, pageNumber:1, sourceHeight:100, sourceWidth:100, data:documentData, thumbnail:documentData)
	def document1 = new Document(id:1, name:'document1', files: [documentData] as SortedSet, previewImages: [previewImage] as SortedSet)
	def document2 = new Document(id:2, name:'document2', files: [documentData] as SortedSet, previewImages: [previewImage] as SortedSet)
	def document3 = new Document(id:3, name:'document3', files: [documentData] as SortedSet, previewImages: [previewImage] as SortedSet)
	@Shared
	def parent1 = new Folder(id:4, name:'parent1')
	def parent2 = new Folder(id:5, name:'parent2')

	def setup() {
		controller.authService = authService
		controller.documentService = documentService
		controller.folderService = folderService
		controller.notificationService = notificationService
		controller.tenantService = tenantService
		controller.metaClass.message = { LinkedHashMap arg1-> 'this is stupid' }

		mockDomain(Group, [group1, group2])
		mockDomain(Folder, [folder1, folder2, folder3, parent1, parent2])
		mockDomain(Document, [document1, document2, document3])

		Document.metaClass.getTags = {-> null }
		Folder.metaClass.getTags = {-> null }

		folder1.group = group1
		folder2.group = group1
		folder3.group = group2

		parent1.group = group1
		parent2.group = group2

		document1.group = group1
		document2.group = group1
		document3.group = group1

		document2.folder = folder1
		folder1.addToDocuments(document2)

		parent1.addToChildren(folder1)
		folder1.parent = parent1
	}

	def cleanup() {
		Document.metaClass.getTags = null
		Folder.metaClass.getTags = null
	}

	def "create should throw an AssertError when given invalid input"() {
		given:
		controller.params.groupId = groupId
		when:
		controller.create()
		then:
		0 * folderService.createFolder(_, _, _)
		thrown(AssertionError)
		where:
		groupId  << ['9', null]
	}

	def "create should render errors return by createFolder"() {
		given:
		controller.params.groupId = '1'
		controller.params.name = '   new folder   '
		when:
		controller.create()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		1 * folderService.createFolder(group1, null, 'new folder') >> { group, parent, name->
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
		controller.params.groupId = '1'
		controller.params.name = '   new folder2   '
		when:
		controller.create()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		1 * folderService.createFolder(group1, null, 'new folder2') >> { group, parent, name->
			def folder = new Folder(id:4, group:group, name:name, parent:parent)
			folder
		}
		1 * notificationService.success(_, _)
		results.folder.id == 4
		results.folder.name == 'new folder2'
		results.folder.group.id == group1.id
		results.folder.group.name == group1.name
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
		1 * notificationService.success(_, _)
	}

	def "list should pass parent, pagination and filter to folderService filter then return the results as JSON"() {
		given:
		controller.params.folderId = folderId
		controller.params.filter = filter
		controller.params.putAll(pagination)
		when:
		controller.list()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		1 * folderService.filter(parent, _, filter) >> [results:[folder1, folder2], total:2]
		results.folders[0].id == folder1.id
		results.folders[0].name == folder1.name
		results.folders[1].id == folder2.id
		results.folders[1].name == folder2.name
		results.folderTotal == 2
		where:
		filter << [null, 'filter']
		folderId << [null, '4']
		parent << [null, parent1]
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
		1 * notificationService.success(_, _)
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

	def "removeDocument should removeDocumentFromFolder when given valid data"() {
		given:
		controller.params.folderId = '1'
		controller.params.documentId = '2'
		when:
		controller.removeDocument()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		1 * folderService.removeDocumentFromFolder(document2)
		1 * notificationService.success(_, _)
	}

	def "addFolder should throw an AssertionError when passed invalid data"() {
		given:
		controller.params.parentId = parentId
		controller.params.childId = childId
		controller.params.currentParentId = currentParentId
		when:
		controller.addFolder()
		then:
		0 * folderService.addChildToFolder(_, _)
		thrown(AssertionError)
		where:
		parentId | childId    | currentParentId
		'9'      | '1'        | '4'             // Bad parentId
		null     | '1'        | '4'             // Bad parentId
		'5'      | '9'        | '4'             // Bad childId
		'5'      | null       | '4'             // Bad childId
		'5'      | '1'        | '5'             // Incorrect currentParentId
		'5'      | '1'        | null            // Incorrect currentParentId
	}

	def "addFolder should addDocumentToFolder when given valid data"() {
		given:
		controller.params.parentId = '5'
		controller.params.folderId = '1'
		controller.params.currentParentId = '4'
		when:
		controller.addFolder()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		1 * folderService.addChildToFolder(parent2, folder1)
		1 * notificationService.success(_, _)
	}

	def "update should throw an AssertError when given invalid input"() {
		given:
		controller.params.folderId = folderId
		when:
		controller.update()
		then:
		0 * folderService.renameFolder(_, _)
		thrown(AssertionError)
		where:
		folderId  << ['9', null]
	}

	def "update should render errors return by createFolder"() {
		given:
		controller.params.folderId = '1'
		controller.params.name = ''
		when:
		controller.update()
		def results = JSON.parse(mockResponse.contentAsString)
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
		controller.params.folderId = '1'
		controller.params.name = '   new folder2   '
		when:
		controller.update()
		def results = JSON.parse(mockResponse.contentAsString)
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
		controller.removeFolder()
		then:
		0 * folderService.removeChildFromFolder(_)
		thrown(AssertionError)
		where:
		folderId | parentId
		'9'      | '2'      // Bad folderId
		null     | '2'      // Bad folderId
		'2'      | '9'      // Bad parentId
		'2'      | null     // Bad parentId
		'2'      | '2'      // Bad current folder
	}

	def "removeFolder should removeDocumentFromFolder when given valid data"() {
		given:
		controller.params.folderId = folder1.id
		controller.params.parentId = folder1.parent.id
		when:
		controller.removeFolder()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		1 * folderService.removeChildFromFolder(folder1)
		1 * notificationService.success(_, _)
	}

	def 'pin should use the folderService to pin the folder'() {
		given:
		controller.params.folderId = folder1.id
		when:
		controller.pin()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		1 * folderService.pin(folder1)
		1 * notificationService.success(_, _)
	}

	def 'unpin should use the folderService to unpin the folder'() {
		given:
		controller.params.folderId = folder1.id
		when:
		controller.unpin()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		1 * folderService.unpin(folder1)
		1 * notificationService.success(_, _)
	}

	def "show should call the default folder_list document_list and folder_ancestry"() {
		given:
		controller.params.folderId = parent1.id
		when:
		controller.show()
		def results = JSON.parse(mockResponse.contentAsString)
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
		given:
		controller.params.folderId = null
		when:
		controller.flag()
		then:
		thrown(AssertionError)
	}

	def 'flag requires the flag name'() {
		given:
		controller.params.folderId = folder1.id
		controller.params.flag = null
		when:
		controller.flag()
		then:
		thrown(AssertionError)
	}

	def 'flag requires the flag is a tenant flag'() {
		given:
		controller.params.folderId = folder1.id
		controller.params.flag = 'notaflag'
		when:
		controller.flag()
		then:
		1 * tenantService.getTenantConfigList('flag') >> ['Waiting']
		thrown(AssertionError)
	}

	def 'flag requires the user can manager folders'() {
		given:
		controller.params.folderId = folder1.id
		controller.params.flag = 'notaflag'
		when:
		controller.flag()
		then:
		1 * tenantService.getTenantConfigList('flag') >> ['Waiting']
		authService.canManageFolders(folder1.group) >> false
		thrown(AssertionError)
	}

	def 'flag returns success after tagging the folder'() {
		def tagAdded = null
		given:
		controller.params.folderId = folder1.id
		controller.params.flag = 'Waiting'
		folder1.metaClass.addTag = { String tag -> tagAdded = tag }
		when:
		controller.flag()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		tagAdded == 'Waiting'
		1 * tenantService.getTenantConfigList('flag') >> ['Waiting']
		authService.canManageFolders(folder1.group) >> true
		1 * notificationService.success(_, _)
	}

	def 'unflag requires the folderId'() {
		given:
		controller.params.folderId = null
		when:
		controller.unflag()
		then:
		thrown(AssertionError)
	}

	def 'unflag requires the flag name'() {
		given:
		controller.params.folderId = folder1.id
		controller.params.flag = null
		when:
		controller.unflag()
		then:
		thrown(AssertionError)
	}

	def 'unflag requires the flag is a tenant flag'() {
		given:
		controller.params.folderId = folder1.id
		controller.params.flag = 'notaflag'
		when:
		controller.unflag()
		then:
		1 * tenantService.getTenantConfigList('flag') >> ['Waiting']
		thrown(AssertionError)
	}

	def 'unflag requires the user can manager folders'() {
		given:
		controller.params.folderId = folder1.id
		controller.params.flag = 'notaflag'
		when:
		controller.unflag()
		then:
		1 * tenantService.getTenantConfigList('flag') >> ['Waiting']
		authService.canManageFolders(folder1.group) >> false
		thrown(AssertionError)
	}

	def 'unflag returns success if the folder did not have the tag'() {
		given:
		controller.params.folderId = folder1.id
		controller.params.flag = 'Waiting'
		folder1.metaClass.getTags = { -> [] }
		when:
		controller.unflag()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		1 * tenantService.getTenantConfigList('flag') >> ['Waiting']
		authService.canManageFolders(folder1.group) >> true
		1 * notificationService.success(_, _)
	}

	def 'unflag returns success after removing the tag from the folder'() {
		def tagRemoved = null
		given:
		controller.params.folderId = folder1.id
		controller.params.flag = 'Waiting'
		folder1.metaClass.getTags = { -> ['Waiting'] }
		folder1.metaClass.removeTag = { String tag -> tagRemoved = tag }
		folder1.metaClass.getFlags = { -> [] }
		when:
		controller.unflag()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		tagRemoved == 'Waiting'
		1 * tenantService.getTenantConfigList('flag') >> ['Waiting']
		authService.canManageFolders(folder1.group) >> true
		1 * notificationService.success(_, _)
	}
}
