package us.paperlesstech

import us.paperlesstech.handlers.Handler

class UploadService {

	static transactional = true

	Handler handlerChain
	def tagService

	Document upload(String name, byte[] data, String contentType, List<String> tags) {
		MimeType mimeType = MimeType.find {it.id == contentType}

		if (mimeType) {
			try {
				Document document = new Document()
				def documentData = new DocumentData(mimeType: mimeType, data: data)
				handlerChain.importFile(document: document, documentData: documentData)

				assert document.files.size() == 1
				document.save()

				tags?.each {
					tagService.addDocumentTag(document, it)
				}

				log.info "Saved document ${document.id}"
				return document
			} catch (Exception e) {
				log.error("Unable to save uploaded document", e)
			}
		}

		return null
	}
}
