package us.paperlesstech

import grails.converters.JSON
import us.paperlesstech.flea.PdfPrinter

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
		boolean printed = false

		if (printer && document) {
			def printData = fileService.getBytes(document.files.first())

			printed = PdfPrinter.printByteArray(deviceType: printer.deviceType, host: printer.host,
					port: printer.port, document: printData)
		}

		if (printed) {
			render([status: "success"] as JSON)
		} else {
			render([status: "error"] as JSON)
		}
	}
}
