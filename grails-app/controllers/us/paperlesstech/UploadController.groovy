package us.paperlesstech

import grails.converters.JSON
import us.paperlesstech.helpers.FileHelpers
import us.paperlesstech.nimble.Group

class UploadController {
	static allowedMethods = [save:'POST']

	def authService
	def uploadService

	def save(Long folderId, Long groupId) {
		def results = []
		def folder = Folder.load(folderId)
		def group = folder?.group ?: Group.get(groupId)

		if (group) {
			request.getMultiFileMap().each { inputName, files->
				files.each { mpf ->
					def is = mpf.inputStream
					is.withStream {
						def documents = uploadService.uploadInputStream(is, group, mpf.originalFilename, mpf.contentType, folder)

						if (documents) {
							documents.each { document ->
								def url = g.createLink(controller:"document", action:"sign", params:[documentId:document.id])
								def thumbnail_url = g.createLink(controller:"document", action:"thumbnail", params:[documentId:document.id, pageNumber:1, documentDataId:document.previewImage(1).thumbnail.id])
								results.add(document.asMap())

							}
						} else {
							response.status = 500
							def error = g.message(code:"document-vault.upload.error.unsupportedfile", args:[FileHelpers.getExtension(mpf.originalFilename)])
							results.add([name:mpf.originalFilename, size:0, error:error])
						}
					}
				}
			}
		} else {
			def error = g.message(code:"document-vault.upload.error.missinggroup")
			results.add([name:"", error:error])
		}

		render(text:results as JSON, contentType:"text/plain")
	}

	def savePcl() {
		def bytes = params.data.bytes
		def documents

		try {
			def group = authService.getGroupsWithPermission([DocumentPermission.Upload]).find { it }
			assert group, "The user must be able to upload to at least one group"
			def now = new Date()
			def fileName = String.format("%tF %tT.pcl", now, now)
			documents = uploadService.uploadDocument(bytes, group, fileName, MimeType.PCL)
		} catch (Throwable e) {
			log.error "Unable to save uploaded document", e
		}

		if (documents) {
			response.status = 200
			documents.each { document ->
				render "Document ${document.id} saved\n"
				log.info "Saved document ${document.id}"
			}
		} else {
			response.status = 500
			render "Error saving file\n"
		}
	}
}
