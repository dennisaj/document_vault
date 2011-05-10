package us.paperlesstech.handlers

import java.util.Map;

import org.apache.commons.logging.LogFactory

import us.paperlesstech.DocumentData

class HandlerChain {
	org.apache.commons.logging.Log log = LogFactory.getLog(getClass())

	def businessLogicService
	def handlers

	void importFile(Map input) {
		handle("importFile", input)
	}

	void generatePreview(Map input) {
		handle("generatePreview", input)
	}

	void print(Map input) {
		handle("print", input)
	}

	void sign(Map input) {
		handle("sign", input)
	}

	private def handle(String methodName, Map input) {
		def data = input.documentData
		assert data instanceof DocumentData, "Handler methods must pass in documentData"

		def handler = handlers.find { it.properties.handlerFor.contains(data.mimeType) }
		assert handler instanceof Handler, "No Handlers handle ${data.mimeType}"

		log.debug "Executing $methodName on $handler"

		def businessMethod = "before${data.mimeType.toString()}${methodName.capitalize()}"
		if (businessLogicService?.metaClass?.respondsTo(businessLogicService, businessMethod)) {
			log.debug "calling $businessLogicService.$businessMethod"
			businessLogicService?."$businessMethod"(input)
		} else {
			log.debug "$businessLogicService.$businessMethod does not exist"
		}

		assert handler.respondsTo(methodName)
		def result = handler."$methodName"(input)

		businessMethod = "after${data.mimeType.toString()}$methodName"
		if (businessLogicService?.metaClass?.respondsTo(businessLogicService, businessMethod)) {
			log.debug "calling $businessLogicService.$businessMethod"
			businessLogicService?."$businessMethod"(input)
		} else {
			log.debug "$businessLogicService.$businessMethod does not exist"
		}

		result
	}
}
