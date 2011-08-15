package us.paperlesstech

import grails.converters.JSON
import us.paperlesstech.flea.PdfPrinter

class PrintQueueController {
	def authService
	def fileService

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
