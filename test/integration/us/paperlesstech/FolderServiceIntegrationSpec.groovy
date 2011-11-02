package us.paperlesstech

import grails.plugin.spock.IntegrationSpec
import us.paperlesstech.nimble.Group

class FolderServiceIntegrationSpec extends IntegrationSpec {
	FolderService service
	AuthService authService = Mock()

	Document document1
	Document document2
	Document document3
	Document document4
	DocumentData dd
	Group group
	Folder folder1
	Folder folder2
	Folder folder3
	Folder parent1

	def setup() {
		service = new FolderService()
		service.authService = authService
		Document.authService = authService
		Folder.authService = authService

		dd = new DocumentData(mimeType:MimeType.PNG, fileSize:1, fileKey:'1234abc')
		dd.save(flush:true)
		group = new Group(name:'group')
		group.save(flush:true)
		parent1 = new Folder(name:'parent1', group:group)

		folder1 = new Folder(name:'folder1', group:group, parent:parent1)
		folder2 = new Folder(name:'folder2', group:group, parent:parent1)
		folder3 = new Folder(name:'folder3', group:group)

		document1 = new Document(group:group)
		document1.addToFiles(dd)
		document2 = new Document(group:group)
		document2.addToFiles(dd)
		document3 = new Document(group:group)
		document3.addToFiles(dd)
		document4 = new Document(group:group)
		document4.addToFiles(dd)
		document3.save(failOnError:true)

		folder1.addToDocuments(document1)
		folder2.addToDocuments(document4)
		folder3.addToDocuments(document2)

		parent1.addToChildren(folder1)
		parent1.addToChildren(folder2)

		folder1.save(failOnError:true)
		folder2.save(failOnError:true)
		folder3.save(failOnError:true)

		parent1.save(failOnError:true)
	}

	def "deleteFolder should remove all of its documents before deleting the folder"() {
		given:
		def folderId = folder1.id
		when:
		service.deleteFolder(folder1)
		then:
		1 * authService.canManageFolders(group) >> true
		!Folder.get(folderId)
		!Document.get(document1.id).folder
		parent1.children.size() == 1
	}

	def "deleteFolder should remove all of its children before deleting the folder"() {
		given:
		def folderId = parent1.id
		def children = parent1.children
		when:
		service.deleteFolder(parent1)
		then:
		1 * authService.canManageFolders(group) >> true
		!Folder.get(folderId)
		children.every { it.parent == null }
	}

	def "addDocumentToFolder should move the document from one folder to another"() {
		when:
		def outDocument = service.addDocumentToFolder(folder2, document1)
		then:
		1 * authService.canManageFolders(document1.group) >> true
		outDocument.id == document1.id
		!folder1.documents.contains(outDocument)
		outDocument.folder == folder2
		folder2.documents.contains(outDocument)
	}

	def "addDocumentToFolder should work if the document is not in a current folder"() {
		when:
		def outDocument = service.addDocumentToFolder(folder1, document3)
		then:
		1 * authService.canManageFolders(document3.group) >> true
		outDocument.id == document3.id
		outDocument.folder == folder1
		folder1.documents.contains(outDocument)
	}

	def "removeDocumentFromFolder should set the document's folder to null"() {
		given:
		when:
		def outDocument = service.removeDocumentFromFolder(document1)
		then:
		1 * authService.canManageFolders(document1.group) >> true
		outDocument.id == document1.id
		!folder1.documents.contains(outDocument)
		!outDocument.folder
	}

	def "filter should return all folders when the user has one of the group permissions"() {
		when:
		def result = service.filter(parent1, [max:10, offset:0, sort:'name', order:'asc'], '')
		then:
		1 * authService.checkGroupPermission(DocumentPermission.ManageFolders, parent1.group) >> true
		result.results.size() == 2
		result.results.contains(folder1)
		result.results.contains(folder2)
		result.total == 2
	}

	def "filter should return only folders where a user has permission to the contained documents when the user lacks permission to the whole group"() {
		when:
		def result = service.filter(parent1, [max:10, offset:0, sort:'name', order:'asc'], '')
		then:
		4 * authService.checkGroupPermission(_, parent1.group) >> false
		1 * authService.getIndividualDocumentsWithPermission(_, parent1.group) >> [document4.id]
		result.results.size() == 1
		!result.results.contains(folder1)
		result.results.contains(folder2)
		result.total == 1
	}

	def "filter should return folders without a parent when parent is null"() {
		when:
		def result = service.filter(null, [max:10, offset:0, sort:'name', order:'asc'], '')
		then:
		0 * authService.checkGroupPermission(_, _)
		1 * authService.getIndividualDocumentsWithPermission(_, null) >> [document2.id]
		result.results.size() == 1
		result.results.contains(folder3)
		result.total == 1
	}

	def "search should return folders regardless of their respective parent"() {
		when:
		def result = service.search([max:10, offset:0, sort:'name', order:'asc'], 'folder')
		then:
		0 * authService.checkGroupPermission(_, _)
		1 * authService.getIndividualDocumentsWithPermission(_, null) >> []
		1 * authService.getGroupsWithPermission(_) >> ([group] as SortedSet)
		result.results.size() == 3
		result.results.contains(folder1)
		result.results.contains(folder2)
		result.results.contains(folder3)
		result.total == 3
	}
}
