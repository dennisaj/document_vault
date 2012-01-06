package us.paperlesstech

import grails.validation.ValidationException
import spock.lang.Shared
import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.User
import spock.lang.Specification
import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import us.paperlesstech.nimble.Profile

@TestFor(FolderService)
@Mock([Folder, PinnedFolder, Document, DocumentData, PreviewImage, Group, User, Profile])
class FolderServiceSpec extends Specification {
	AuthService authService = Mock()
	FolderService service

	Document document1
	Document document2
	Document document3

	Folder parent1
	Folder parent2
	Folder parent3

	Folder folder1
	Folder folder2
	Folder folder3

	User user

	def setup() {
		service = new FolderService()
		service.authService = authService

		document1 = UnitTestHelper.createDocument()
		document2 = UnitTestHelper.createDocument(group: document1.group)
		document3 = UnitTestHelper.createDocument()

		parent1 = new Folder(name: 'parent1', group: document1.group).save(flush: true, failOnError: true)
		parent2 = new Folder(name: 'parent2', group: document2.group).save(flush: true, failOnError: true)
		parent3 = new Folder(name: 'parent3', group: document1.group).save(flush: true, failOnError: true)

		folder1 = new Folder(name: 'folder1', group: document1.group).save(flush: true, failOnError: true)
		folder2 = new Folder(name: 'folder2', group: document1.group).save(flush: true, failOnError: true)
		folder3 = new Folder(name: 'folder3', group: document1.group).save(flush: true, failOnError: true)

		folder1.addToDocuments(document1)
		folder1.save()
		folder2.addToDocuments(document2)
		folder2.save()

		parent1.addToChildren(folder1)
		parent1.save()

		user = UnitTestHelper.createUser()
	}

	def "createFolder should require a group"() {
		when:
		service.createFolder(null, 'name')
		then:
		thrown(AssertionError)
	}

	def "createFolder should require the given parent to be in the given group"() {
		def group = new Group(name: 'new group').save(flush: true, failOnError: true)
		def parent = new Folder(name: 'parent', group: group)
		when:
		service.createFolder(document1.group, 'name', parent)
		then:
		1 * authService.canManageFolders(document1.group) >> true
		thrown(AssertionError)
	}

	def "createFolder should throw an AssertionError when the user lacks the ManageFolders permission"() {
		when:
		service.createFolder(document3.group, 'name')
		then:
		1 * authService.canManageFolders(document3.group) >> false
		thrown(AssertionError)
	}

	def "createFolder should return errors if validation fails"() {
		when:
		def savedFolder = service.createFolder(document1.group, '')
		then:
		1 * authService.canManageFolders(document1.group) >> true
		savedFolder.errors
	}

	def "createFolder should set the parent if it is passed in"() {
		given:
		parent1.parent = null

		when:
		def newFolder = service.createFolder(parent1.group, 'new folder', parent1)

		then:
		1 * authService.canManageFolders(parent1.group) >> true
		newFolder.parent == parent1
		newFolder.name == 'new folder'
		parent1.children.contains(newFolder)
	}

	def "deleteFolder should require a folder"() {
		when:
		service.deleteFolder(null)
		then:
		thrown(AssertionError)
	}

	def "deleteFolder should throw an Assertion error when the user lacks the correct permissions"() {
		when:
		service.deleteFolder(folder1)
		then:
		1 * authService.canManageFolders(folder1.group) >> false
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

	def "addDocumentToFolder should throw an Assertion error when the user lacks the ManageFolders permission"() {
		when:
		service.addDocumentToFolder(folder1, document2)
		then:
		1 * authService.canManageFolders(document2.group) >> false
		thrown(AssertionError)
	}

	def "addDocumentToFolder should return if the destination folder is the same as the current folder"() {
		when:
		def outDocument = service.addDocumentToFolder(document2.folder, document2)
		then:
		0 * authService.canManageFolders(document2.group)
		outDocument.is document2
	}

	def "addDocumentToFolder should check the ManageFolders permission if the document has a folder and continue if the user is permitted"() {
		when:
		service.addDocumentToFolder(folder1, document2)
		then:
		1 * authService.canManageFolders(document2.group) >> true
	}

	def "removeDocumentFromFolder should require a document"() {
		when:
		service.removeDocumentFromFolder(null)
		then:
		thrown(AssertionError)
	}

	def "removeDocumentFromFolder should check the ManageFolders permission if the document has a folder and throw an AssertError if the user is denied"() {
		when:
		service.removeDocumentFromFolder(document1)
		then:
		1 * authService.canManageFolders(document1.group) >> false
		thrown(AssertionError)
	}

	def "addChildToFolder should require a parent"() {
		when:
		service.addChildToFolder(null, folder1)
		then:
		thrown(AssertionError)
	}

	def "addChildToFolder should require a child"() {
		when:
		service.addChildToFolder(parent3, null)
		then:
		thrown(AssertionError)
	}

	def "addChildToFolder should require the parent to be in the same group as the child folder"() {
		when:
		service.addChildToFolder(parent2, folder1)
		then:
		thrown(AssertionError)
	}

	def "addChildToFolder should return if the parent folder is the same as the current parent"() {
		when:
		def outDocument = service.addChildToFolder(folder1.parent, folder1)
		then:
		0 * authService.canManageFolders(folder1.parent)
		outDocument.is folder1
	}

	def "addChildToFolder should throw an Assertion error when the user lacks the ManageFolders permission"() {
		when:
		service.addChildToFolder(parent3, folder1)
		then:
		1 * authService.canManageFolders(parent3.group) >> false
		thrown(AssertionError)
	}

	def "addChildToFolder should check the ManageFolders permission if the document has a folder and continue if the user is permitted"() {
		given:
		parent3.parent = null
		assert folder1.parent != parent3
		assert !folder1.children
		assert !parent3.children

		when:
		service.addChildToFolder(parent3, folder1)

		then:
		1 * authService.canManageFolders(parent3.group) >> true
	}

	def "addChildToFolder should freak out when trying to add a parent to its child"() {
		given:
		folder1.parent.parent = null

		when:
		service.addChildToFolder(folder1, folder1.parent)

		then:
		1 * authService.canManageFolders(folder1.group) >> true
		thrown(ValidationException)
	}

	def "renameFolder should throw an AssertionError when the user lacks the ManageFolders permission"() {
		when:
		service.renameFolder(folder1, 'name')
		then:
		1 * authService.canManageFolders(folder1.group) >> false
		thrown(AssertionError)
	}

	def "renameFolder should return errors if validation fails"() {
		when:
		def savedFolder = service.renameFolder(folder1, '')
		then:
		1 * authService.canManageFolders(folder1.group) >> true
		savedFolder.errors
	}

	def "renameFolder should set the name when no error occur"() {
		when:
		def savedFolder = service.renameFolder(folder1, 'new folder')
		then:
		1 * authService.canManageFolders(folder1.group) >> true
		folder1.name == 'new folder'
	}

	def "removeChildFromFolder should require a child"() {
		when:
		def savedFolder = service.removeChildFromFolder(null)
		then:
		thrown(AssertionError)
	}

	def "removeChildFromFolder should throw an AssertionError when the user lacks the ManageFolders permission"() {
		when:
		def savedFolder = service.removeChildFromFolder(folder1)
		then:
		1 * authService.canManageFolders(folder1.group) >> false
		thrown(AssertionError)
	}

	def "removeChildFromFolder should return the child when the parent is null"() {
		when:
		def savedFolder = service.removeChildFromFolder(folder2)
		then:
		1 * authService.canManageFolders(folder2.group) >> true
		savedFolder.is folder2
	}

	def "removeChildFromFolder should remove the child from its parent when there are not other problems"() {
		given:
		def parent = folder1.parent
		when:
		def savedFolder = service.removeChildFromFolder(folder1)
		then:
		1 * authService.canManageFolders(folder1.group) >> true
		savedFolder.parent == null
		!parent.children.contains(savedFolder)
	}

	def "pin should do nothing if the folder is already pinned"() {
		def pinned = new PinnedFolder(user: user, folder: folder1)
		pinned.save(flush: true, failOnError: true)

		when:
		service.pin(folder1)

		then:
		1 * authService.authenticatedUser >> user
		PinnedFolder.list().size() == 1
		PinnedFolder.list()[0].folder == folder1
	}

	def "pin should pin the folder for the user and up the count"() {
		def pinned = new PinnedFolder(user: user, folder: folder1)
		pinned.save(flush: true, failOnError: true)

		when:
		service.pin(folder2)

		then:
		1 * authService.authenticatedUser >> user
		PinnedFolder.list().size() == 2
		PinnedFolder.list()[1].folder == folder2
	}

	def "unpin should do nothing if the folder is not pinned"() {
		when:
		service.unpin(folder1)

		then:
		1 * authService.authenticatedUser >> user
		PinnedFolder.list().size() == 0
	}

	def "unpin should delete the matching pinnedFolder"() {
		def pinned = new PinnedFolder(user: user, folder: folder1)
		pinned.save(flush: true, failOnError: true)

		when:
		service.unpin(folder1)

		then:
		1 * authService.authenticatedUser >> user
		PinnedFolder.list().size() == 0
	}

	def "ancestry should return an empty list for a folder with no parents"() {
		folder1.parent = null

		expect:
		service.ancestry(folder1) == []
	}

	def "ancestry should return the ancestry in order of highest level first"() {
		folder1.parent = parent1
		parent1.parent = parent2
		parent2.parent = parent3

		expect:
		service.ancestry(folder1) == [parent3, parent2, parent1]
	}
}
