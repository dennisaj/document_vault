package us.paperlesstech

import us.paperlesstech.helpers.FileHelpers
import us.paperlesstech.helpers.PclDocument
import us.paperlesstech.helpers.PclInfo
import us.paperlesstech.nimble.Group

class UploadService {
	static transactional = true

	def authServiceProxy
	def businessLogicService
	def fileService
	def handlerChain

	List<Document> uploadInputStream(InputStream is, Group group, String name, String contentType) {
		MimeType mimeType = MimeType.getMimeType(mimeType: contentType, fileName: name)

		if (mimeType) {
			return uploadDocument(is?.bytes, group, name, mimeType)
		}

		return null
	}

	List<Document> uploadDocument(byte[] data, Group group, String name, MimeType mimeType) {
		assert authServiceProxy.canUpload(group)
		assert data
		assert mimeType
		def pclInfo

		def save = { PclDocument pclDocument ->
			Document document = new Document()
			document.group = group
			document.name = FileHelpers.chopExtension(name)
			handlerChain.importFile(document: document, documentData: new DocumentData(mimeType: mimeType), bytes: data,
					pclDocument: pclDocument)
			assert document.save(flush: true)

			log.info "Saved document ${document.id}"
			return document
		}

		try {
			if (mimeType == MimeType.PCL) {
				pclInfo = new PclInfo()
				pclInfo.parse(data: data)

				return pclInfo.documents.collect { pclDocument ->
					save.call(pclDocument)
				}
			} else {
				return [save.call()]
			}
		} catch (Exception e) {
			log.error("Unable to save uploaded document", e)
		}

		return null
	}
}
