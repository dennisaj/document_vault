package us.paperlesstech.handlers

import java.util.Map

import org.apache.commons.logging.LogFactory

import us.paperlesstech.DocumentData
import us.paperlesstech.PreviewImage

class HandlerChain extends Handler {
	org.apache.commons.logging.Log log = LogFactory.getLog(getClass())

	def businessLogicService
	def handlers

	@Override
	void importFile(Map input) {
		handle("importFile", input)
	}

	@Override
	void generatePreview(Map input) {
		handle("generatePreview", input)
	}

	@Override
	void print(Map input) {
		handle("print", input)
	}

	@Override
	void sign(Map input) {
		handle("sign", input)
	}

	@Override
	def retrievePreview(Map input) {
		handle("retrievePreview", input)
	}

	@Override
	def download(Map input) {
		handle("download", input)
	}

	private def handle(String methodName, Map input) {
		def document = getDocument(input)
		def data = input.documentData ?: document.files.first()
		assert data, "A DocumentData object must be available for all Handler calls"

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
