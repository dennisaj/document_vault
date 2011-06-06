package us.paperlesstech

import grails.plugins.nimble.core.Group
import us.paperlesstech.helpers.FileHelpers

class UploadService {
	static transactional = true

	def authService
	def handlerChain
	def businessLogicService

	Document upload(Group group, String name, byte[] data, String contentType) {
		MimeType mimeType = MimeType.getMimeType(mimeType: contentType, fileName: name)

		if (mimeType) {
			return upload(group, name, data, mimeType)
		}

		return null
	}

	Document upload(Group group, String name, byte[] data, MimeType mimeType) {
		assert authService.canUpload(group)

		try {
			Document document = new Document()
			document.group = group
			document.name = FileHelpers.chopExtension(name)
			def documentData = new DocumentData(mimeType: mimeType, data: data)
			handlerChain.importFile(document: document, documentData: documentData)

			assert document.files.size() == 1
			assert document.save(flush: true)

			log.info "Saved document ${document.id}"
			return document
		} catch (Exception e) {
			log.error("Unable to save uploaded document", e)
		}

		return null
	}
}
