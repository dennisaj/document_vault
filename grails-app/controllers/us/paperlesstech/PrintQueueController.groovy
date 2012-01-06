package us.paperlesstech

import grails.converters.JSON

class PrintQueueController {
	def authService
	def handlerChain
	def notificationService
	def preferenceService

	def push(Long documentId, Long printerId, boolean addNotes) {
		def document = Document.load(documentId)
		def printer = Printer.load(printerId)
		boolean printed = false

		if (printer && document) {
			printed = handlerChain.print(document:document, printer:printer, addNotes:addNotes)
		}

		if (printed) {
			render([notification:notificationService.success('document-vault.api.printqueue.push.success')] as JSON)
		} else {
			render([notification:notificationService.error('document-vault.api.printqueue.push.error')] as JSON)
		}
	}

	def details(Long documentId) {
		def document = Document.load(documentId)
		assert document

		render([
			printing: [
				documentId: document.id,
				printers: Printer.list()*.asMap(),
				showNotesOption: authService.canNotes(document),
				defaultPrinterId: preferenceService.getPreference(authService.authenticatedUser, PreferenceService.DEFAULT_PRINTER)
			]
		] as JSON)
	}
}
