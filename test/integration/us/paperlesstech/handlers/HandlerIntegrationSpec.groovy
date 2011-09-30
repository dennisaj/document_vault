package us.paperlesstech.handlers

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.core.io.ClassPathResource

import us.paperlesstech.Document
import us.paperlesstech.DocumentData
import us.paperlesstech.DomainIntegrationSpec
import us.paperlesstech.FileService
import us.paperlesstech.MimeType
import us.paperlesstech.Note

class HandlerIntegrationSpec extends BaseHandlerSpec {
	def defaultImageHandlerService
	FileService fileService
	GrailsApplication grailsApplication = Mock()
	def handler = new Handler()
	def line = [a:[x:0,y:0], b:[x:100,y:100]]

	@Override
	def setup() {
		handler.authService = authService
		handler.fileService = fileService
		handler.grailsApplication = grailsApplication
		defaultImageHandlerService.authService = authService

		def config = new ConfigObject()
		config.document_vault.document.note.defaultWidth = 800
		config.document_vault.document.note.defaultHeight = 600
		config.document_vault.document.note.defaultMimeType = MimeType.PNG

		grailsApplication.metaClass.getConfig = {-> config }
	}

	def "saveNotes should throw an exception if a note contains neither text or lines"() {
		given:
		def notes = [[lines:[], text:""]]
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
		thrown AssertionError
	}

	def "saveNotes should not have DocumentData if no lines are passed in"() {
		given:
		def notes = [[lines:[], text:"this is some text"]]
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
		document.notes.first().data == null
		document.notes.first().note == "this is some text"
	}

	def "saveNotes should set the note field to null if no text is passed in"() {
		given:
		def notes = [[lines:[line]]]
		when:
		def document = new Document(group: DomainIntegrationSpec.group)
		def documentData = new DocumentData(mimeType: MimeType.PNG)
		def bytes = new ClassPathResource("test.png").getFile().bytes
		def input = [document: document, documentData: documentData, bytes: bytes]

		defaultImageHandlerService.importFile(input)
		document = document.save()
		input = [document: document, documentData: document.files.first(), notes:notes]
		handler.saveNotes(input)

		then:
		document.notes.size() == 1
		document.notes.first().data.mimeType == MimeType.PNG
		document.notes.first().note == null
	}

	def "saveNotes should save both the text and lines"() {
		given:
		def notes = [[lines:[line], text:"this is some text"]]
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
		document.notes.first().data.mimeType == MimeType.PNG
		document.notes.first().note == "this is some text"
	}
}
