package us.paperlesstech

import grails.converters.JSON

class PrintQueueController {
	def authService
	def handlerChain

	def push = {
		def printer = Printer.load(params.printerId)
		def document = Document.load(params.documentId)
		boolean printed = false

		if (printer && document) {
			printed = handlerChain.print(document: document, printer: printer)
		}

		if (printed) {
			render([status: "success"] as JSON)
		} else {
			render([status: "error"] as JSON)
		}
	}
}
