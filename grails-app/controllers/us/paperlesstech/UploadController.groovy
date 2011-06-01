package us.paperlesstech

import grails.converters.JSON

class UploadController {
	static allowedMethods = [save: "POST", saveAjax: "POST"]
	static navigation = [[action: 'index', isVisible: {authService.canUploadAny()}, order: 10, title: 'Upload']]

	def authService
	def uploadService

	def index = {
	}

	def save = {
		def green = []
		def red = []
		request.getMultiFileMap().each {inputName, files ->
			files.each {
				def document = uploadService.upload(it.originalFilename, it.bytes, it.contentType)

				if (document) {
					green += document
				} else {
					red += it.originalFilename
				}
			}
		}

		if (red) {
			flash.red = "Document(s) " + red.join(", ") + " could not be saved"
		}

		if (green) {
			flash.green = "Document(s) " + green.join(", ") + " were saved"
		}

		redirect(view:"index")
	}

	def ajaxSave = {
		def f = request.getFile('file')
		if (!f.empty) {
			def document = uploadService.upload(f.originalFilename, f.bytes, f.contentType)

			if (document) {
				def html = g.render(template:"link", model:[name:document.toString(), id:document.id])
				render (text:[name:document.toString(), size:f.bytes.length, html:html] as JSON, contentType:"text/plain")
				return
			} else {
				def html = g.render(template:"/notsaved", model:[message:g.message(code:"document-vault.error.upload.failure", args:[f.originalFilename])])
				render (text:[html:html] as JSON, contentType:"text/plain")
				return
			}
		}
		render (text:[:] as JSON, contentType:"text/plain")
	}

	def savePcl = {
		boolean success = false
		def document

		try {
			def now = new Date()
			def fileName = String.format("%tF %tT.pcl", now, now)
			document = uploadService.upload(fileName, params.data, MimeType.PCL, null)
			success = true
		} catch (Exception e) {
			log.error("Unable to save uploaded document", e)
		}

		if (success) {
			response.status = 200
			render "Document ${document.id} saved\n"
			log.info "Saved document ${document.id}"
		} else {
			response.status = 500
			render "Error saving file\n"
		}
	}
}
