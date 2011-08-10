package us.paperlesstech

import grails.converters.JSON
import us.paperlesstech.helpers.FileHelpers
import us.paperlesstech.nimble.Group

class UploadController {
	static allowedMethods = [save: "POST", saveAjax: "POST"]
	static navigation = [[action: 'index', isVisible: {authService.canUploadAny()}, order: 10, title: 'Upload']]

	def authService
	def uploadService

	def index = {
		[groups:authService.getGroupsWithPermission([DocumentPermission.Upload])]
	}

	def ajaxSave = {
		params.ajax = true

		save(params)
	}

	def save = {
		def isAjax = params.ajax || request.xhr
		def results = []
		def group = Group.get(params.int('group'))

		if (group) {
			request.getMultiFileMap().each {inputName, files->
				files.each { mpf ->
					def is = mpf.inputStream
					is.withStream {
						def documents = uploadService.uploadInputStream(is, group, mpf.originalFilename, mpf.contentType)

						if (documents) {
							documents.each { document ->
								def url = g.createLink(controller:"document", action:"show", params:[documentId:document.id])
								def thumbnail_url = g.createLink(controller:"document", action:"thumbnail", params:[documentId:document.id, pageNumber:1, documentDataId:document.previewImage(1).thumbnail.id])
								results.add([name:document.toString(), size:document.files.first().fileSize, url:url,thumbnail_url:thumbnail_url])
							}
						} else {
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

		isAjax ? render(text:results as JSON, contentType:"text/plain") : chain(action:"index", model:[results:results])
	}

	def savePcl = {
		def documents

		try {
			def group = authService.getGroupsWithPermission([DocumentPermission.Upload]).find { it }
			assert group, "The user must be able to upload to at least one group"
			def now = new Date()
			def fileName = String.format("%tF %tT.pcl", now, now)
			documents = uploadService.uploadDocument(params.data?.bytes, group, fileName, MimeType.PCL)
		} catch (Throwable e) {
			log.error("Unable to save uploaded document", e)
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
