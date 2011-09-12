package us.paperlesstech

import grails.converters.JSON

class PrintQueueController {
	def authService
	def handlerChain
	def preferenceService

	def push = {
		def printer = Printer.load(params.printerId)
		def document = Document.load(params.documentId)
		def addNotes = params.boolean('addNotes')
		boolean printed = false

		if (printer && document) {
			printed = handlerChain.print(document:document, printer:printer, addNotes:addNotes)
		}

		if (printed) {
			render([status:"success"] as JSON)
		} else {
			render([status:"error"] as JSON)
		}
	}

	def printWindow = {
		def document = Document.load(params.documentId)
		assert document

		render(template:"printerDialog", model:[document:document], defaultPrinter:preferenceService.getPreference(authService.authenticatedUser, PreferenceService.DEFAULT_PRINTER))
	}
}
