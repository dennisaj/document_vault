package us.paperlesstech.handlers

import org.apache.commons.logging.LogFactory

class HandlerChain extends Handler {
	org.apache.commons.logging.Log log = LogFactory.getLog(getClass())
	def authService
	def businessLogicService
	def handlers

	@Override
	void importFile(Map input) {
		def document = getDocument(input)
		assert authService.canUpload(document.group)

		handle("importFile", input)
	}

	@Override
	void generatePreview(Map input) {
		def document = getDocument(input)
		assert authService.canUpload(document.group) || authService.canSign(document)

		handle("generatePreview", input)
	}

	@Override
	void print(Map input) {
		def document = getDocument(input)
		assert authService.canPrint(document)

		handle("print", input)
	}

	@Override
	void sign(Map input) {
		def document = getDocument(input)
		assert authService.canSign(document)

		handle("sign", input)
	}

	@Override
	def downloadPreview(Map input) {
		def document = getDocument(input)
		assert authService.canView(document) || authService.canSign(document)

		handle("downloadPreview", input)
	}

	@Override
	def download(Map input) {
		def document = getDocument(input)
		assert authService.canView(document) || authService.canSign(document)

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
		
		businessMethod = businessMethod.replaceFirst("before", "after")
		if (businessLogicService?.metaClass?.respondsTo(businessLogicService, businessMethod)) {
			log.debug "calling $businessLogicService.$businessMethod"
			businessLogicService?."$businessMethod"(input)
		} else {
			log.debug "$businessLogicService.$businessMethod does not exist"
		}

		result
	}
}
