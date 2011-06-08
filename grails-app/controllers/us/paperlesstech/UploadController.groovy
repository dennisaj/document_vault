package us.paperlesstech

import us.paperlesstech.helpers.FileHelpers
import grails.converters.JSON
import grails.plugins.nimble.core.Group

class UploadController {
	static allowedMethods = [save: "POST", saveAjax: "POST"]
	static navigation = [[action: 'index', isVisible: {authService.canUploadAny()}, order: 10, title: 'Upload']]

	def authService
	def uploadService

	def index = {
		[groups:authService.getGroupsWithPermission(DocumentPermission.Upload)]
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
				files.each {
					def document = uploadService.upload(group, it.originalFilename, it.bytes, it.contentType)

					if (document) {
						def url = g.createLink(controller:"document", action:"show", params:[documentId:document.id])
						results.add([name:document.toString(), size:document.files.first().data.length, url:url])
					} else {
						def error = g.message(code:"document-vault.upload.error.unsupportedfile", args:[FileHelpers.getExtension(it.originalFilename)])
						results.add([name:it.originalFilename, size:0, error:error])
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
		boolean success = false
		def document

		try {
			def group = authService.getGroupsWithPermission(DocumentPermission.Upload).find { it }
			assert group, "The user must be able to upload to at least one group"
			def now = new Date()
			def fileName = String.format("%tF %tT.pcl", now, now)
			document = uploadService.upload(group, fileName, params.data?.bytes, MimeType.PCL)
		} catch (Throwable e) {
			log.error("Unable to save uploaded document", e)
		}

		if (document) {
			response.status = 200
			render "Document ${document.id} saved\n"
			log.info "Saved document ${document.id}"
		} else {
			response.status = 500
			render "Error saving file\n"
		}
	}
}
