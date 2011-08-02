package us.paperlesstech.handlers

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.core.io.ClassPathResource

import us.paperlesstech.Document
import us.paperlesstech.DocumentData
import us.paperlesstech.DomainIntegrationSpec
import us.paperlesstech.FileService
import us.paperlesstech.MimeType

class HandlerIntegrationSpec extends BaseHandlerSpec {
	def defaultImageHandlerService
	FileService fileService
	GrailsApplication grailsApplication = Mock()
	def handler = new Handler()
	def line = [a:[x:0,y:0], b:[x:100,y:100]]

	@Override
	def setup() {
		handler.authServiceProxy = authServiceProxy
		handler.fileService = fileService
		handler.grailsApplication = grailsApplication
		defaultImageHandlerService.authServiceProxy = authServiceProxy

		def config = new ConfigObject()
		config.document_vault.document.note.defaultWidth = 800
		config.document_vault.document.note.defaultHeight = 600
		config.document_vault.document.note.defaultMimeType = MimeType.PNG

		grailsApplication.metaClass.getConfig = {-> config }
	}

	def "saveNotes should create a new DocumentData when an invalid DocumentData id is passed"() {
		given:
		def notes = [(-1):[line]]
		when:
		def document = new Document(group: DomainIntegrationSpec.group)
		def documentData = new DocumentData(mimeType: MimeType.PNG)
		def bytes = new ClassPathResource("test.png").getFile().bytes
		def input = [document: document, documentData: documentData, bytes: bytes]

		defaultImageHandlerService.importFile(input)
		document = document.save()
		input = [document: document, documentData: document.files.first(), notes: notes]
		handler.saveNotes(input)

		then:
		document.notes.size() == 1
		document.notes.first().mimeType == MimeType.PNG
	}

	def "saveNotes should update an existing DocumentData when a valid DocumentData id is passed"() {
		given:
		def notes = [(-1):[line]]
		when:
		def document = new Document(group: DomainIntegrationSpec.group)
		def documentData = new DocumentData(mimeType: MimeType.PNG)
		def bytes = new ClassPathResource("test.png").getFile().bytes
		def input = [document: document, documentData: documentData, bytes: bytes]

		defaultImageHandlerService.importFile(input)
		document = document.save()
		input = [document: document, documentData: document.files.first(), notes: notes]
		handler.saveNotes(input)
		input.notes = [(document.notes.first().id):[line]]
		handler.saveNotes(input)

		then:
		document.notes.size() == 1
		document.notes.first().mimeType == MimeType.PNG
		document.notes.first().id != (notes.keySet()).find{ it }
	}

	def "saveNotes should error if the document note does not belong to the current document"() {
		given:
		def notes = [(-1):[line]]
		when:
		def document = new Document(group: DomainIntegrationSpec.group)
		def documentData = new DocumentData(mimeType: MimeType.PNG)
		def bytes = new ClassPathResource("test.png").getFile().bytes
		def input = [document: document, documentData: documentData, bytes: bytes]

		defaultImageHandlerService.importFile(input)
		document = document.save()
		input = [document: document, documentData: document.files.first(), notes: notes]
		handler.saveNotes(input)
		input.notes = [(document.files.first().id):[line]]
		handler.saveNotes(input)

		then:
		thrown AssertionError
	}
}
