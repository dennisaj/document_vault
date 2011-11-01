package us.paperlesstech

import grails.converters.JSON
import grails.plugin.spock.ControllerSpec

import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException

import us.paperlesstech.handlers.Handler

class NoteControllerSpec extends ControllerSpec {
	Handler handlerChain = Mock()
	NotificationService notificationService = Mock()

	def documentData = new DocumentData(id:1, pages:4, dateCreated: new Date())
	def note1 = new Note(id:1, document:document1, dateCreated: new Date() - 1)
	def note2 = new Note(id:2, document:document1, dateCreated: new Date())
	def note3 = new Note(id:3, document:document2, dateCreated: new Date(), data:documentData)
	def document1 = new Document(id:1, files:([documentData] as SortedSet), notes:([note1, note2] as SortedSet))
	def document2 = new Document(id:2, files:([documentData] as SortedSet), notes:([note3] as SortedSet))

	def setup() {
		controller.metaClass.createLink = { LinkedHashMap arg1 -> 'this is stupid' }
		controller.handlerChain = handlerChain
		controller.notificationService = notificationService

		mockDomain(Document, [document1, document2])
		mockDomain(DocumentData, [documentData])
		mockDomain(Note, [note1, note2, note3])
	}

	def "saveText should throw an AssertionError when given an invalid documentId"() {
		given:
		controller.params.documentId = null
		when:
		controller.saveText()
		then:
		thrown(AssertionError)
	}

	def "saveText should throw an AssertionError when given an invalid value"() {
		given:
		controller.params.documentId = '1'
		controller.params.value = null
		when:
		controller.saveText()
		then:
		thrown(AssertionError)
	}

	def "saveText should throw an AssertionError when given an invalid page"() {
		given:
		controller.params.documentId = '1'
		controller.params.value = 'valid'
		controller.params.pageNumber = '100'
		when:
		controller.saveText()
		then:
		thrown(AssertionError)
	}

	def "saveText should default pageNumbers below 0 or empty pageNumbers to 0"() {
		given:
		controller.params.documentId = '1'
		controller.params.value = value
		controller.params.pageNumber = pageNumber
		controller.params.left = left
		controller.params.top = top
		when:
		controller.saveText()
		then:
		1 * handlerChain.saveNotes([document:document1, notes:[[text:value, left:left, top:top, pageNumber:0]]])
		where:
		value = 'valid'
		left = 100
		top = 100
		pageNumber << ['-1', null]
	}

	def "saveText should call handerChain's saveNotes when given valid values"() {
		given:
		controller.params.documentId = '1'
		controller.params.value = 'valid'
		controller.params.pageNumber = '1'
		when:
		controller.saveText()
		then:
		1 * notificationService.success(_)
	}

	def "saveLines should throw an NullPointerException when given null notes"() {
		given:
		controller.params.notes = null
		controller.params.documentId = null
		when:
		controller.saveLines()
		then:
		thrown(NullPointerException)
	}

	def "saveLines should throw an ConverterException when given '' notes"() {
		given:
		controller.params.notes = ''
		controller.params.documentId = null
		when:
		controller.saveLines()
		then:
		thrown(ConverterException)
	}

	def "saveLines should throw an AssertionError when given empty notes"() {
		given:
		controller.params.notes = '[]'
		controller.params.documentId = null
		when:
		controller.saveLines()
		then:
		thrown(AssertionError)
	}

	def "saveLines should throw an AssertionError when given an invalid documentId"() {
		given:
		controller.params.notes = '[lines]'
		controller.params.documentId = null
		when:
		controller.saveLines()
		then:
		thrown(AssertionError)
	}

	def "saveLines should call handlerChain's saveNotes when given valid data"() {
		given:
		def outputNotes = null
		controller.params.notes = notes
		controller.params.documentId = '1'
		when:
		controller.saveLines()
		then:
		1 * handlerChain.saveNotes(_) >> { LinkedHashMap arg1-> outputNotes = arg1.notes }
		1 * notificationService.success(_)
		outputNotes.size() == 2
		where:
		notes = "['lines','more lines','']"
	}

	def "list should throw an AssertionError when given an invalid documentId"() {
		given:
		controller.params.documentId = null
		when:
		controller.list()
		then:
		thrown(AssertionError)
	}

	def "list should return the notes for a document when given a valid documentId"() {
		given:
		controller.params.documentId = '1'
		when:
		controller.list()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		results.notes.size() == 2
	}

	def "list should include a url if a note contains DocumentData"() {
		given:
		controller.params.documentId = '2'
		when:
		controller.list()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		results.notes.size() == 1
		results.notes.'3'.url
	}

	def "download should throw an AssertionError when given an invalid noteDataId"() {
		given:
		controller.params.documentId = '1'
		controller.params.noteDataId = null
		when:
		controller.download()
		then:
		thrown(AssertionError)
	}

	def "download should return a 404 when given invalid documentId or noteDataId"() {
		given:
		controller.params.documentId = documentId
		controller.params.noteDataId = noteDataId
		when:
		controller.download()
		then:
		mockResponse.status == 404
		where:
		documentId << [null, 1]
		noteDataId << [3, 3]
	}

	def "download should call handlerChain's downloadNote when given valid input"() {
		given:
		controller.metaClass.cache = { LinkedHashMap arg1 -> 'this is stupid' }
		controller.params.documentId = '2'
		controller.params.noteDataId = '1'
		when:
		controller.download()
		then:
		1 * handlerChain.downloadNote([document:document2, note:note3]) >> { LinkedHashMap-> ['filename', new ByteArrayInputStream([1] as byte[]), MimeType.PNG.downloadContentType, 1] }
		mockResponse.status == 200
	}
}
