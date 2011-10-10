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
	Bucket bucket

	def setup() {
		service = new FolderService()
		service.authService = authService

		dd = new DocumentData(mimeType:MimeType.PNG, fileSize:1, fileKey:'1234abc')
		dd.save(flush:true)
		group = new Group(name:'group')
		group.save(flush:true)

		folder1 = new Folder(name:'folder1', group:group)
		folder2 = new Folder(name:'folder2', group:group)
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

		folder1.save(failOnError:true)
		folder2.save(failOnError:true)
		folder3.save(failOnError:true)

		bucket = new Bucket(id:1, name:'bucket1', group:group)
		bucket.addToFolders(folder1)
		bucket.addToFolders(folder2)
		bucket.save(failOnError:true)
	}

	def "deleteFolder should remove all of its documents before deleting the folder"() {
		given:
		1 * authService.canFolderDelete(group) >> true
		def folderId = folder1.id
		when:
		service.deleteFolder(folder1)
		then:
		!Folder.get(folderId)
		!Document.get(document1.id).folder
	}

	def "addDocumentToFolder should move the document from one folder to another"() {
		given:
		1 * authService.canFolderMoveInTo(document1.group) >> true
		1 * authService.canFolderMoveOutOf(document1.group) >> true
		when:
		def outDocument = service.addDocumentToFolder(folder2, document1)
		then:
		outDocument.id == document1.id
		!folder1.documents.contains(outDocument)
		outDocument.folder == folder2
		folder2.documents.contains(outDocument)
	}

	def "addDocumentToFolder should work if the document is not in a current folder"() {
		given:
		1 * authService.canFolderMoveInTo(document3.group) >> true
		0 * authService.canFolderMoveOutOf(_)
		when:
		def outDocument = service.addDocumentToFolder(folder1, document3)
		then:
		outDocument.id == document3.id
		outDocument.folder == folder1
		folder1.documents.contains(outDocument)
	}

	def "removeDocumentFromFolder should set the document's folder to null"() {
		given:
		1 * authService.canFolderMoveOutOf(document1.group) >> true
		when:
		def outDocument = service.removeDocumentFromFolder(document1)
		then:
		outDocument.id == document1.id
		!folder1.documents.contains(outDocument)
		!outDocument.folder
	}

	def "search should return all folders when the user has one of the group permissions"() {
		given:
		1 * authService.checkGroupPermission(DocumentPermission.FolderCreate, bucket.group) >> true
		when:
		def result = service.search(bucket, [max:10, offset:0, sort:'name', order:'asc'])
		then:
		result.results.size() == 2
		result.results.contains(folder1)
		result.results.contains(folder2)
		result.total == 2
	}

	def "search should return only folders where a user has permission to containing documents when the user lacks permission to the whole group"() {
		given:
		6 * authService.checkGroupPermission(_, bucket.group) >> false
		1 * authService.getIndividualDocumentsWithPermission(_, bucket.group) >> [document4.id]
		when:
		def result = service.search(bucket, [max:10, offset:0, sort:'name', order:'asc'])
		then:
		result.results.size() == 1
		!result.results.contains(folder1)
		result.results.contains(folder2)
		result.total == 1
	}
}
