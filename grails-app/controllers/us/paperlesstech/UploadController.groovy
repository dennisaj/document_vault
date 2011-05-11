package us.paperlesstech

import grails.converters.JSON
import org.apache.shiro.SecurityUtils

class UploadController {
	static allowedMethods = [save: "POST", saveAjax: "POST"]
	static navigation = [[action: 'index', isVisible: {SecurityUtils.subject.isPermitted("upload:*")}, order: 10, title: 'Upload']]

	def businessLogicService
	def handlerChain
	def tagService
	def uploadService

	def index = {
		[tag:params.tag, recentTags:tagService.getRecentTags()]
	}

	def save = {
		def tag = params.tag?.trim()
		def green = []
		def red = []
		request.getMultiFileMap().each {inputName, files ->
			files.each {
				def document = uploadService.upload(it.originalFilename, it.bytes, it.contentType, tag ? [tag] : null)

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
		def tag = params.tag?.trim()
		if (!f.empty) {
			def document = uploadService.upload(f.originalFilename, f.bytes, f.contentType, tag ? [tag] : null)

			if (document) {
				render (text:[name:document.toString(), size:f.bytes.length] as JSON, contentType:"text/plain")
				return
			}
		}
		render (text:[:] as JSON, contentType:"text/plain")
	}

	def savePcl = {
		try {
			Document document = new Document()
			def documentData = new DocumentData(mimeType: MimeType.PCL, data: params.data)
			handlerChain.importFile(document: document, documentData: documentData)

			assert document.files.size() == 1
			document.save()

			if (businessLogicService.addTags(document)) {
				document.save()
			}

			response.status = 200
			render "Document ${document.id} saved\n"
			log.info "Saved document ${document.id}"
		} catch (Exception e) {
			log.error("Unable to save uploaded document", e)
			response.status = 500
			render "Error saving file\n"
		}
	}
}
