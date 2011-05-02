package us.paperlesstech

import grails.converters.JSON

import org.springframework.util.MultiValueMap
import org.springframework.web.multipart.MultipartFile

class UploadController {
	static allowedMethods = [save: "POST", saveAjax: "POST"]
	static navigation = [[action:'index', isVisible: {springSecurityService.isLoggedIn()}, order:10, title:'Upload']]
	
	def springSecurityService
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
				def document = uploadService.upload(it.originalFilename, it.bytes, it.contentType, [tag])

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
			def document = uploadService.upload(f.originalFilename, f.bytes, f.contentType, [tag])

			if (document) {
				render (text:[name:document.toString(), size:f.bytes.length] as JSON, contentType:"text/plain")
				return
			}
		}
		render (text:[:] as JSON, contentType:"text/plain")
	}
}
