package us.paperlesstech

import grails.plugin.spock.UnitSpec
import spock.lang.Shared
import us.paperlesstech.nimble.Group

class FolderServiceSpec extends UnitSpec {
	AuthService authService = Mock()
	FolderService service

	Group group1 = new Group(id:1, name:'group1')
	Group group2 = new Group(id:2, name:'group2')
	Document document1 = new Document(id:1)
	Document document2 = new Document(id:2)
	@Shared
	Document document3 = new Document(id:3)
	Folder folder1 = new Folder(id:1, name:'folder1')
	Folder folder2 = new Folder(id:2, name:'folder2')
	Folder folder3 = new Folder(id:3, name:'folder3')

	def setup() {
		mockLogging(FolderService)
		service = new FolderService()
		service.authService = authService

		folder1.group = group1
		folder2.group = group1
		folder3.group = group1
		document1.group = group1
		document2.group = group1
		document3.group = group2

		mockDomain(Document, [document1, document2, document3])
		mockDomain(Folder, [folder1, folder2, folder3])
		mockDomain(Group, [group1, group2])

		document1.folder = folder1
		document2.folder = folder2
		folder1.addToDocuments(document1)
		folder2.addToDocuments(document2)
	}

	def "createFolder should require a group"() {
		when:
		service.createFolder(null, 'name', document3)
		then:
		thrown(AssertionError)
	}

	def "createFolder should require the given document to be in the given group"() {
		when:
		service.createFolder(group1, 'name', document)
		then:
		thrown(AssertionError)
		where:
		document << [null, document3]
	}

	def "createFolder should throw an AssertionError when the user lacks the FolderCreate permission"() {
		given:
		1 * authService.canFolderCreate(document3.group) >> false
		when:
		service.createFolder(document3.group, 'name', document3)
		then:
		thrown(AssertionError)
	}

	def "createFolder should throw an AssertionError if the initialDocument is in a folder and the user can't move it out"() {
		given:
		1 * authService.canFolderCreate(group1) >> true
		1 * authService.canFolderMoveOutOf(group1) >> false
		when:
		service.createFolder(group1, 'name', document2)
		then:
		thrown(AssertionError)
	}

	def "createFolder should not check canFolderMoveOutOf if the initialDocument isn't in a folder"() {
		given:
		1 * authService.canFolderCreate(document3.group) >> true
		when:
		service.createFolder(document3.group, 'name', document3)
		then:
		0 * authService.canFolderMoveOutOf(_)
	}

	def "createFolder should return errors if validation fails"() {
		given:
		1 * authService.canFolderCreate(document3.group) >> true
		when:
		def savedFolder = service.createFolder(document3.group, '', document3)
		then:
		savedFolder.errors
	}

	def "deleteFolder should require a folder"() {
		when:
		service.deleteFolder(null)
		then:
		thrown(AssertionError)
	}

	def "deleteFolder should throw an Assertion error when the user lacks the correct permissions"() {
		given:
		1 * authService.canFolderDelete(folder1.group) >> false
		when:
		service.deleteFolder(folder1)
		then:
		thrown(AssertionError)
	}

	def "addDocumentToFolder should require a folder"() {
		when:
		service.addDocumentToFolder(null, document1)
		then:
		thrown(AssertionError)
	}

	def "addDocumentToFolder should require a document"() {
		when:
		service.addDocumentToFolder(folder1, null)
		then:
		thrown(AssertionError)
	}

	def "addDocumentToFolder should require the folder to be in the same group as the destination folder"() {
		when:
		service.addDocumentToFolder(folder1, document3)
		then:
		thrown(AssertionError)
	}

	def "addDocumentToFolder should throw an Assertion error when the user lacks the canFolderMoveInTo permission"() {
		given:
		1 * authService.canFolderMoveInTo(document2.group) >> false
		when:
		service.addDocumentToFolder(folder1, document2)
		then:
		thrown(AssertionError)
	}

	def "addDocumentToFolder should return if the destination folder is the same as the current folder"() {
		given:
		0 * authService.canFolderMoveInTo(_)
		when:
		def outDocument = service.addDocumentToFolder(document2.folder, document2)
		then:
		outDocument.is document2
	}

	def "addDocumentToFolder should not check the canFolderMoveOutOf permission if the document has no folder"() {
		given:
		document2.folder = null
		1 * authService.canFolderMoveInTo(document2.group) >> true
		when:
		service.addDocumentToFolder(folder1, document2)
		then:
		0 * authService.canFolderMoveOutOf(_)
	}

	def "addDocumentToFolder should check the canFolderMoveOutOf permission if the document has a folder and throw an AssertError if the user is denied"() {
		given:
		1 * authService.canFolderMoveInTo(document2.group) >> true
		1 * authService.canFolderMoveOutOf(document2.group) >> false
		when:
		service.addDocumentToFolder(folder1, document2)
		then:
		thrown(AssertionError)
	}

	def "addDocumentToFolder should check the canFolderMoveOutOf permission if the document has a folder and continue if the user is permitted"() {
		given:
		1 * authService.canFolderMoveInTo(document2.group) >> true
		when:
		service.addDocumentToFolder(folder1, document2)
		then:
		1 * authService.canFolderMoveOutOf(document2.group) >> true
	}

	def "removeDocumentFromFolder should require a document"() {
		when:
		service.removeDocumentFromFolder(null)
		then:
		thrown(AssertionError)
	}

	def "removeDocumentFromFolder should not check the canFolderMoveOutOf permission if the document has no folder"() {
		when:
		service.removeDocumentFromFolder(document3)
		then:
		0 * authService.canFolderMoveOutOf(_)
	}

	def "removeDocumentFromFolder should check the canFolderMoveOutOf permission if the document has a folder and throw an AssertError if the user is denied"() {
		given:
		1 * authService.canFolderMoveOutOf(document1.group) >> false
		when:
		service.removeDocumentFromFolder(document1)
		then:
		thrown(AssertionError)
	}

	def "removeDocumentFromFolder should check the canFolderMoveOutOf permission if the document has a folder and continue if the user is permitted"() {
		when:
		service.removeDocumentFromFolder(document1)
		then:
		1 * authService.canFolderMoveOutOf(document1.group) >> true
	}
}
