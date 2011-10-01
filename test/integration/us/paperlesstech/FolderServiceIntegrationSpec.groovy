package us.paperlesstech

import grails.plugin.spock.IntegrationSpec
import us.paperlesstech.nimble.Group

class FolderServiceIntegrationSpec extends IntegrationSpec {
	FolderService service
	AuthService authService = Mock()

	Document document1
	Document document2
	Document document3
	DocumentData dd
	Group group
	Folder folder1
	Folder folder2

	def setup() {
		service = new FolderService()
		service.authService = authService

		dd = new DocumentData(mimeType:MimeType.PNG, fileSize:1, fileKey:'1234abc')
		dd.save(flush:true)
		group = new Group(name:'group')
		group.save(flush:true)

		folder1 = new Folder(name:'folder1', group:group)
		folder2 = new Folder(name:'folder2', group:group)

		document1 = new Document(group:group)
		document1.addToFiles(dd)
		document2 = new Document(group:group)
		document2.addToFiles(dd)
		document3 = new Document(group:group)
		document3.addToFiles(dd)
		document3.save()

		folder1.addToDocuments(document1)
		folder1.addToDocuments(document2)

		folder1.save(flush:true)
		folder2.save(flush:true)
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
		!Document.get(document2.id).folder
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
}
