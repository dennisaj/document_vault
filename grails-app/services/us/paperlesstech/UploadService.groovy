package us.paperlesstech

import us.paperlesstech.helpers.FileHelpers
import us.paperlesstech.helpers.PclDocument
import us.paperlesstech.helpers.PclInfo
import us.paperlesstech.nimble.Group

class UploadService {
	static transactional = true

	def authService
	def businessLogicService
	def fileService
	def handlerChain

	List<Document> uploadInputStream(InputStream is, Group group, String name, String contentType, Folder folder=null) {
		MimeType mimeType = MimeType.getMimeType(mimeType:contentType, fileName:name)

		if (mimeType) {
			return uploadDocument(is?.bytes, group, name, mimeType, folder)
		}

		return []
	}

	List<Document> uploadDocument(byte[] data, Group group, String name, MimeType mimeType, Folder folder=null) {
		assert authService.canUpload(group)
		assert !folder || authService.canManageFolders(group)
		assert data
		assert mimeType
		def pclInfo

		def save = { PclDocument pclDocument ->
			Document document = new Document()
			document.group = group
			document.folder = folder
			document.name = FileHelpers.chopExtension(name)
			handlerChain.importFile(document:document, documentData:new DocumentData(mimeType:mimeType), bytes:data, pclDocument:pclDocument)

			folder?.addToDocuments(document)
			assert document.save(flush:true)
			folder?.save(flush:true)

			return document
		}

		try {
			if (mimeType == MimeType.PCL) {
				pclInfo = new PclInfo()
				pclInfo.parse(data:data)

				return pclInfo.documents.collect { pclDocument ->
					save.call(pclDocument)
				}
			} else {
				return [save.call()]
			}
		} catch (Exception e) {
			log.error "Unable to save uploaded document", e
		}

		return []
	}
}
