package us.paperlesstech

import us.paperlesstech.helpers.FileHelpers

class UploadService {
	static transactional = true

	def authService
	def handlerChain
	def businessLogicService
	def tagService

	Document upload(String name, byte[] data, String contentType, List<String> tags) {
		MimeType mimeType = MimeType.getMimeType(mimeType: contentType, fileName: name)

		if (mimeType) {
			return upload(name, data, mimeType, tags)
		}

		return null
	}

	Document upload(String name, byte[] data, MimeType mimeType, List<String> tags) {
		assert authService.canUploadAny()

		def fileGroup = authService?.authenticatedUser?.groups?.find { group ->
			group.permissions?.find { permission ->
				permission?.target?.contains("document:upload")
			}
		}
		assert fileGroup

		try {
			Document document = new Document()
			document.group = fileGroup
			document.name = FileHelpers.chopExtension(name)
			def documentData = new DocumentData(mimeType: mimeType, data: data)
			handlerChain.importFile(document: document, documentData: documentData)

			assert document.files.size() == 1
			document.save()

			tags?.each {
				tagService.addDocumentTag(document, it)
			}

			if (businessLogicService.addTags(document)) {
				document.save()
			}

			log.info "Saved document ${document.id}"
			return document
		} catch (Exception e) {
			log.error("Unable to save uploaded document", e)
		}

		return null
	}
}
