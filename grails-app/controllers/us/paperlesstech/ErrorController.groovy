package us.paperlesstech

import grails.converters.JSON

class ErrorController {

	def index = {
		def error = [:]
		try {
			error.statusCode = request.'javax.servlet.error.status_code'
			if (grails.util.Environment.current == grails.util.Environment.DEVELOPMENT) {
				error.message = request.'javax.servlet.error.message'?.encodeAsHTML()
				error.servlet = request.'javax.servlet.error.servlet_name'

				def exception = request.exception
				if (exception) {
					error.exception = [
						message: exception.message?.encodeAsHTML(),
						cause: exception.cause?.message?.encodeAsHTML(),
						'class': exception.className,
						line: exception.lineNumber,
						stacktrace: exception.stackTraceLines.join()
					]
				}
			}
		} catch (Exception e) {
			println e
		}

		render(error as JSON)
	}
}
