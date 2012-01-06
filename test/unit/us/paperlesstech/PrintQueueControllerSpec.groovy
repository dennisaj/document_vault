package us.paperlesstech

import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import spock.lang.Specification

import us.paperlesstech.handlers.Handler
import us.paperlesstech.nimble.Group

@TestFor(PrintQueueController)
@Mock([Printer, Document, DocumentData, PreviewImage, Group])
class PrintQueueControllerSpec extends Specification {
	AuthService authService = Mock()
	Handler handlerChain = Mock()
	NotificationService notificationService = Mock()
	PreferenceService preferenceService = Mock()

	def setup() {
		controller.authService = authService
		controller.handlerChain = handlerChain
		controller.preferenceService = preferenceService
		controller.notificationService = notificationService

		def document = UnitTestHelper.createDocument()
		def printer = new Printer(id:1, host:'localhost', deviceType:'printer', name:'Willy', port:666).save(failOnError:true)
	}

	def "printWindow should throw an AssertionError when an invalid documentId is given"() {
		when:
		controller.details(null)
		then:
		thrown(AssertionError)
		0 * notificationService.success(_)
	}

	def "printWindow should retrieve the requested document and also get the printer preference"() {
		when:
		controller.details(1L)
		def results = JSON.parse(response.contentAsString)
		then:
		1 * preferenceService.getPreference(_, PreferenceService.DEFAULT_PRINTER) >> '1'
		results.printing.documentId == 1
	}

	def "push should call print if a valid printer and document are given"() {
		when:
		def ret = controller.push(documentId, printerId, addNotes)
		then:
		1 * handlerChain.print([document:Document.get(documentId), printer:Printer.get(printerId), addNotes:Boolean.valueOf(addNotes)]) >> true
		1 * notificationService.success(_)
		where:
		documentId = 1L
		printerId = 1L
		addNotes << [true, false]
	}

	def "push should not call print if a valid printer and document are not given"() {
		when:
		def ret = controller.push(documentId, printerId, false)
		0 * handlerChain.print(_)
		then:
		1 * notificationService.error(_)
		where:
		documentId << [1L, 2L, 2L]
		printerId << [2L, 1L, 2L]
	}
}

