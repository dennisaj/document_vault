package us.paperlesstech

import grails.converters.JSON
import grails.plugin.spock.ControllerSpec
import us.paperlesstech.handlers.Handler

class PrintQueueControllerSpec extends ControllerSpec {
	AuthService authService = Mock()
	Handler handlerChain = Mock()
	PreferenceService preferenceService = Mock()

	def document = new Document(id:1)
	def printer = new Printer(id:1)

	def setup() {
		controller.authService = authService
		controller.handlerChain = handlerChain
		controller.preferenceService = preferenceService

		mockDomain(Document, [document])
		mockDomain(Printer, [printer])
	}

	def "printWindow should throw an AssertionError when an invalid documentId is given"() {
		given:
		controller.params.documentId = null
		when:
		controller.printWindow()
		then:
		thrown(AssertionError)
	}

	def "printWindow should retrieve the requested document and also get the printer preference"() {
		given:
		controller.params.documentId = 1
		1 * preferenceService.getPreference(_, PreferenceService.DEFAULT_PRINTER) >> '1'
		when:
		def model = controller.printWindow()
		then:
		model.document.id == 1
		controller.renderArgs.template == 'printerDialog'
	}

	def "push should call print if a valid printer and document are given"() {
		given:
		controller.params.documentId = 1
		controller.params.printerId = 1
		controller.params.addNotes = addNotes
		// We need to include the []'s because Spock explodes without them
		1 * handlerChain.print([document:document, printer:printer, addNotes:Boolean.valueOf(addNotes)]) >> true
		when:
		def ret = controller.push()
		then:
		JSON.parse(mockResponse.contentAsString).status == 'success'
		where:
		addNotes << ['true', 'false']
	}

	def "push should not call print if a valid printer and document are not given"() {
		given:
		controller.params.documentId = documentId
		controller.params.printerId = printerId
		controller.params.addNotes = 'false'
		// We need to include the []'s because Spock explodes without them
		0 * handlerChain.print(_)
		when:
		def ret = controller.push()
		then:
		JSON.parse(mockResponse.contentAsString).status == 'error'
		where:
		documentId << [1, 2, 2]
		printerId << [2, 1, 2]
	}
}
