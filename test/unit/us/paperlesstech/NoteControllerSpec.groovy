package us.paperlesstech

import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import org.junit.Test

import spock.lang.Specification
import us.paperlesstech.handlers.Handler
import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.Profile
import us.paperlesstech.nimble.User

@TestFor(NoteController)
@Mock([Document, DocumentData, Note, User, Profile, Group, PreviewImage])
class NoteControllerSpec extends Specification {
	Handler handlerChain = Mock()
	NotificationService notificationService = Mock()

	def setup() {
		controller.handlerChain = handlerChain
		controller.notificationService = notificationService

		def documentData = new DocumentData(id:1, pages:4, dateCreated: new Date(), mimeType:MimeType.PNG, fileSize:1, fileKey:'1234').save(failOnError:true)
		def note1 = new Note(id:1, dateCreated: new Date() - 2)
		def note2 = new Note(id:2, dateCreated: new Date() - 1)
		def note3 = new Note(id:3, dateCreated: new Date(), data:documentData)

		def document1 = UnitTestHelper.createDocument()
		def document2 = UnitTestHelper.createDocument()
		def user = UnitTestHelper.createUser()

		note1.user = user
		note2.user = user
		note3.user = user

		document1.addToNotes(note1)
		document1.addToNotes(note2)
		document2.addToNotes(note3)

		document1.save(failOnError:true)
		document2.save(failOnError:true)
	}

	def "saveText should throw an AssertionError when given an invalid documentId"() {
		when:
		controller.saveText(null, 'text', 0, 0f, 0f)
		then:
		thrown(AssertionError)
	}

	def "saveText should throw an AssertionError when given an invalid value"() {
		when:
		controller.saveText(1L, null, 0, 0f, 0f)
		then:
		thrown(AssertionError)
	}

	def "saveText should throw an AssertionError when given an invalid page"() {
		when:
		controller.saveText(1L, 'valid', 100, 0f, 0f)
		then:
		thrown(AssertionError)
	}

	def "saveText should default pageNumbers below 0 or empty pageNumbers to 0"() {
		when:
		controller.saveText(documentId, text, pageNumber, left, top)
		then:
		1 * handlerChain.saveNotes([document:Document.get(documentId), notes:[[text:text, left:left, top:top, pageNumber:0]]])
		where:
		documentId = 1L
		text = 'valid'
		left = 100f
		top = 100f
		pageNumber << [-1, null]
	}

	def "saveText should call handerChain's saveNotes when given valid values"() {
		when:
		controller.saveText(1L, 'valid', 1, 0f, 0f)
		then:
		1 * notificationService.success(_)
	}

	def "saveLines should throw an NullPointerException when given null notes"() {
		when:
		controller.saveLines(1L, null)
		then:
		thrown(NullPointerException)
	}

	def "saveLines should throw an ConverterException when given '' notes"() {
		when:
		controller.saveLines(1L, '')
		then:
		thrown(ConverterException)
	}

	def "saveLines should throw an AssertionError when given empty notes"() {
		when:
		controller.saveLines(1L, '[]')
		then:
		thrown(AssertionError)
	}

	def "saveLines should throw an AssertionError when given an invalid documentId"() {
		when:
		controller.saveLines(null, '["lines"]')
		then:
		thrown(AssertionError)
	}

	def "saveLines should call handlerChain's saveNotes when given valid data"() {
		given:
		def outputNotes = null
		when:
		controller.saveLines(1L, notes)
		then:
		1 * handlerChain.saveNotes(_) >> { LinkedHashMap arg1-> outputNotes = arg1.notes }
		1 * notificationService.success(_)
		outputNotes.size() == 2
		where:
		notes = "['lines','more lines','']"
	}

	def "list should throw an AssertionError when given an invalid documentId"() {
		when:
		controller.list(null)
		then:
		thrown(AssertionError)
	}

	def "list should return the notes for a document when given a valid documentId"() {
		when:
		controller.list(1L)
		def results = JSON.parse(response.contentAsString)
		then:
		results.notes.size() == 2
	}

	def "download should throw an AssertionError when given an invalid noteDataId"() {
		when:
		controller.download(1L, null)
		then:
		thrown(AssertionError)
	}

	def "download should return a 404 when given invalid documentId or noteDataId"() {
		when:
		controller.download(documentId, noteDataId)
		then:
		response.status == 404
		where:
		documentId << [null, 1L]
		noteDataId << [3L, 3L]
	}

	def "download should call handlerChain's downloadNote when given valid input"() {
		given:
		controller.metaClass.cache = { LinkedHashMap arg1 -> 'this is stupid' }
		when:
		controller.download(documentId, noteDataId)
		println response.properties
		then:
		1 * handlerChain.downloadNote([document:Document.get(documentId), note:Note.findByData(DocumentData.get(noteDataId))]) >> { LinkedHashMap args ->
			println args
			['filename', new ByteArrayInputStream([1] as byte[]), MimeType.PNG.downloadContentType, 1]
		}
		response.status == 200
		where:
		documentId = 2L
		noteDataId = 1L
	}
}
