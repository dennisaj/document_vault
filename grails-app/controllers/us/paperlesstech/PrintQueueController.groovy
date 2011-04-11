package us.paperlesstech

import grails.converters.JSON

class PrintQueueController {
	def activityLogService
	def springSecurityService

    def scaffold = true

	def pop = {
		def results = PrintQueue.listOrderByDateCreated(max:1, order:"asc")
		if (results.size() > 0) {
			def j = [printer: results[0].printer, document: results[0].document.pdf.data.encodeBase64().toString()]
			results[0].delete()
			render j as JSON
		}

		render [:] as JSON
    }

	def push = {
		def printer = Printer.get(params.printerId)
		def document = Document.get(params.documentId)

		// Documents can be printed if they are signed. This should probably be configurable.
		if (printer && document?.signed) {
			activityLogService.addPrintLog(request, document)
			def queue = new PrintQueue(document:document, printer:printer, user:springSecurityService.currentUser)
			if (queue.save()) {
				render([status:"success"] as JSON)
			}
		}

		render([status:"error"] as JSON)
    }
}
