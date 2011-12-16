package us.paperlesstech

import spock.lang.Shared
import spock.lang.Unroll
import us.paperlesstech.nimble.Group
import spock.lang.Specification
import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import us.paperlesstech.nimble.User
import us.paperlesstech.nimble.Profile

@TestFor(Folder)
@Mock([Folder, PinnedFolder, Document, DocumentData, PreviewImage, Group, User, Profile])
class FolderSpec extends Specification {
	Document document
	
	def parentFolder
	
	def childFolder1
	def childFolder2
	def childFolder3
	
	def grandchildFolder1
	def nonchildFolder1
	
	def setup() {
		document = UnitTestHelper.createDocument()
		parentFolder = new Folder(id: 1, name: 'parent', group: document.group).save(flush: true, failOnError: true)

		childFolder1 = new Folder(id: 2, name: 'child1', group: document.group).save(flush: true, failOnError: true)
		childFolder2 = new Folder(id: 3, name: 'child2', group: document.group).save(flush: true, failOnError: true)
		childFolder3 = new Folder(id: 4, name: 'child3', group: document.group).save(flush: true, failOnError: true)

		grandchildFolder1 = new Folder(id: 5, name: 'grandchild1', group: document.group).save(flush: true, failOnError: true)
		nonchildFolder1 = new Folder(id: 6, name: 'nonchild1', group: document.group).save(flush: true, failOnError: true)
		
		parentFolder.addToChildren(childFolder1)
		parentFolder.addToChildren(childFolder2)
		parentFolder.addToChildren(childFolder3)

		parentFolder.parent = null
		childFolder1.parent = parentFolder
		childFolder2.parent = parentFolder
		childFolder3.parent = parentFolder
		parentFolder.save(flush: true, failOnError: true)

		childFolder1.addToChildren(grandchildFolder1)
		grandchildFolder1.parent = childFolder1
		childFolder1.save(flush: true, failOnError: true)
	}

	def "folders with no documents or name should not be saved"() {
		def folder = new Folder()
		when:
		def result = folder.validate()
		then:
		!result
		folder.errors.hasFieldErrors('name')
	}

	def "deleting a folder should leave the documents"() {
		assert Document.count() == 1
		assert Folder.count() == 6

		given:
		def documentId = document.id
		def folder = new Folder(name:'folder')
		folder.addToDocuments(document)
		folder.save(flush:true)
		when:
		folder.delete(flush:true)
		then:
		Document.get(documentId)
		Folder.count() == 6
	}

	def "a folder should not be able set its parent to a sub folder"() {
		when:
		parentFolder.parent = childFolder1
		childFolder1.addToChildren(parentFolder)
		def result = parentFolder.validate()
		then:
		!result
		parentFolder.errors.hasFieldErrors('parent')
	}

	def "a folder should not be able set its parent to a folder descendant"() {
		when:
		parentFolder.parent = grandchildFolder1
		grandchildFolder1.addToChildren(parentFolder)
		def result = parentFolder.validate()
		then:
		!result
		parentFolder.errors.hasFieldErrors('parent')
	}

	def "a folder should be able set its parent to a nondescendant"() {
		when:
		parentFolder.parent = nonchildFolder1
		nonchildFolder1.addToChildren(parentFolder)
		def result = parentFolder.validate()
		then:
		result
	}
}
