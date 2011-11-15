package us.paperlesstech

import grails.plugin.spock.UnitSpec
import grails.validation.ValidationException
import spock.lang.Shared
import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.User

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

	Folder parent1 = new Folder(id:4, name:'parent1')
	@Shared
	Folder parent2 = new Folder(id:5, name:'parent2')
	Folder parent3 = new Folder(id:5, name:'parent3')

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

		parent1.group = group1
		parent2.group = group2
		parent3.group = group1

		mockDomain(Document, [document1, document2, document3])
		mockDomain(Folder, [folder1, folder2, folder3, parent1, parent2, parent3])
		mockDomain(Group, [group1, group2])

		document1.folder = folder1
		document2.folder = folder2
		folder1.addToDocuments(document1)
		folder2.addToDocuments(document2)

		parent1.addToChildren(folder1)
		folder1.parent = parent1
	}

	def "createFolder should require a group"() {
		when:
		service.createFolder(null, 'name')
		then:
		thrown(AssertionError)
	}

	def "createFolder should require the given parent to be in the given group"() {
		when:
		service.createFolder(group1, parent, 'name')
		then:
		thrown(AssertionError)
		where:
		parent << [null, parent2]
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
		def savedFolder = service.createFolder(group1, '')
		then:
		1 * authService.canManageFolders(group1) >> true
		savedFolder.errors
	}

	def "createFolder should set the parent if it is passed in"() {
		when:
		def savedFolder = service.createFolder(parent1.group, parent1, 'new folder')
		then:
		1 * authService.canManageFolders(parent1.group) >> true
		savedFolder.parent == parent1
		parent1.children.contains(savedFolder)
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
		when:
		service.addChildToFolder(parent3, folder1)
		then:
		1 * authService.canManageFolders(parent3.group) >> true
	}

	def "addChildToFolder should freak out when trying to add a parent to its child"() {
		when:
		service.addChildToFolder(folder1, parent1)
		then:
		1 * authService.canManageFolders(parent3.group) >> true
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
		def user = new User()
		def pinned = new PinnedFolder(user: user, folder: folder1)
		mockDomain(PinnedFolder, [pinned])

		when:
		service.pin(folder1)

		then:
		1 * authService.authenticatedUser >> user
		PinnedFolder.list().size() == 1
		PinnedFolder.list()[0].folder == folder1
	}

	def "pin should pin the folder for the user and up the count"() {
		def user = new User()
		def pinned = new PinnedFolder(user: user, folder: folder1)
		mockDomain(PinnedFolder, [pinned])

		when:
		service.pin(folder2)

		then:
		1 * authService.authenticatedUser >> user
		PinnedFolder.list().size() == 2
		PinnedFolder.list()[1].folder == folder2
	}

	def "unpin should do nothing if the folder is not pinned"() {
		def user = new User()
		mockDomain(PinnedFolder)

		when:
		service.unpin(folder1)

		then:
		1 * authService.authenticatedUser >> user
		PinnedFolder.list().size() == 0
	}

	def "unpin should delete the matching pinnedFolder"() {
		def user = new User()
		def pinned = new PinnedFolder(user: user, folder: folder1)
		mockDomain(PinnedFolder, [pinned])

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
