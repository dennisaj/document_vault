package us.paperlesstech

import grails.converters.JSON

class PrintQueueController {
	def authService
	def fileService

	def pop = {
		def results = PrintQueue.listOrderByDateCreated(max:1, order:"asc")
		if (results.size() > 0) {
			String printData = fileService.getBytes(results[0].document.files.first()).encodeBase64().toString()
			def j = [printer: results[0].printer, document: printData]
			results[0].delete()
			render(j as JSON)

			return
		}

		render([:] as JSON)
	}

	def push = {
		def printer = Printer.load(params.printerId)
		def document = Document.load(params.documentId)

		if (printer && document) {
			def queue = new PrintQueue(document:document, printer:printer, user: authService.authenticatedUser)
			if (queue.save()) {
				render([status:"success"] as JSON)

				return
			}
		}

		render([status:"error"] as JSON)
	}
}
