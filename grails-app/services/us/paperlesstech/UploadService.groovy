package us.paperlesstech

import grails.plugins.nimble.core.Group
import us.paperlesstech.helpers.FileHelpers

class UploadService {
	static transactional = true

	def authService
	def businessLogicService
	def fileService
	def handlerChain

	Document uploadInputStream(InputStream is, Group group, String name, String contentType) {
		MimeType mimeType = MimeType.getMimeType(mimeType: contentType, fileName: name)

		if (mimeType) {
			return uploadDocument(is?.bytes, group, name, mimeType)
		}

		return null
	}

	Document uploadDocument(byte[] data, Group group, String name, MimeType mimeType) {
		assert authService.canUpload(group)
		assert data
		assert mimeType

		try {
			Document document = new Document()
			document.group = group
			document.name = FileHelpers.chopExtension(name)
			handlerChain.importFile(document: document, documentData: new DocumentData(mimeType: mimeType), bytes: data)
			assert document.save(flush: true)

			log.info "Saved document ${document.id}"
			return document
		} catch (Exception e) {
			log.error("Unable to save uploaded document", e)
		}

		return null
	}
}
