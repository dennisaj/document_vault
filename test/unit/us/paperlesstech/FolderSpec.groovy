package us.paperlesstech

import grails.plugin.spock.UnitSpec
import us.paperlesstech.nimble.Group

class FolderSpec extends UnitSpec {
	Document document = new Document(id:1, group:new Group(name:'name'))
	DocumentData documentData = new DocumentData(mimeType:MimeType.PDF, fileSize:1, fileKey:"asdf")

	def setup() {
		mockDomain(Document, [document])
		mockDomain(DocumentData, [documentData])
		mockDomain(Folder)
	}

	def "folders with no documents or name should not be saved"() {
		given:
		mockForConstraintsTests(Folder)
		def folder = new Folder()
		when:
		def result = folder.validate()
		then:
		!result
		folder.errors.hasFieldErrors('documents')
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
		Folder.count() == 0
	}
}
