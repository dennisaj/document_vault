package us.paperlesstech

import grails.converters.JSON

class PrintQueueController {
	def authenticatedService

	def pop = {
		def results = PrintQueue.listOrderByDateCreated(max:1, order:"asc")
		if (results.size() > 0) {
			def j = [printer: results[0].printer, document: results[0].document.pdf.data.encodeBase64().toString()]
			results[0].delete()
			render (j as JSON)
		}

		render ([:] as JSON)
	}

	def push = {
		def printer = Printer.load(params.printerId)
		def document = Document.load(params.documentId)

		if (printer && document) {
			def queue = new PrintQueue(document:document, printer:printer, user: authenticatedService.authenticatedUser)
			if (queue.save()) {
				render([status:"success"] as JSON)
			}
		}

		render([status:"error"] as JSON)
	}
}
