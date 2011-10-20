package us.paperlesstech

import grails.plugin.spock.UnitSpec
import spock.lang.Shared
import spock.lang.Unroll
import us.paperlesstech.nimble.Group

class FolderSpec extends UnitSpec {
	Document document = new Document(id:1, group:new Group(name:'name'))
	DocumentData documentData = new DocumentData(mimeType:MimeType.PDF, fileSize:1, fileKey:"asdf")
	@Shared
	def group = new Group(id:1, name:'group1')
	def parentFolder = new Folder(id:1, name:'parent', group:group)
	@Shared
	def childFolder1 = new Folder(id:2, name:'child1', group:group)
	def childFolder2 = new Folder(id:3, name:'child2', group:group)
	def childFolder3 = new Folder(id:4, name:'child3', group:group)
	@Shared
	def grandchildFolder1 = new Folder(id:5, name:'grandchild1', group:group)
	def nonchildFolder1 = new Folder(id:6, name:'nonchild1', group:group)

	def setup() {
		mockDomain(Group, [group])
		mockDomain(Document, [document])
		mockDomain(DocumentData, [documentData])
		mockDomain(Folder, [parentFolder, childFolder1, childFolder2, childFolder3, grandchildFolder1, nonchildFolder1])

		parentFolder.addToChildren(childFolder1)
		parentFolder.addToChildren(childFolder2)
		parentFolder.addToChildren(childFolder3)

		parentFolder.parent = null
		childFolder1.parent = parentFolder
		childFolder2.parent = parentFolder
		childFolder3.parent = parentFolder

		childFolder1.addToChildren(grandchildFolder1)
		grandchildFolder1.parent = childFolder1
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
		given:
		document.addToFiles(documentData)
		document.save(flush:true)
		def folder = new Folder(name:'folder')
		folder.addToDocuments(document)
		folder.save(flush:true)
		when:
		folder.delete(flush:true)
		then:
		Document.get(1)
		Folder.count() == 6
	}

	@Unroll("Testing if parentFolder can have its parent set to #newParent")
	def "a folder should not be able set its parent to a descendant"() {
		when:
		parentFolder.parent = newParent
		newParent.addToChildren(parentFolder)
		def result = parentFolder.validate()
		then:
		!result
		parentFolder.errors.hasFieldErrors('parent')
		where:
		newParent << [childFolder1, grandchildFolder1]
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
