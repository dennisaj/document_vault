package us.paperlesstech

import grails.converters.JSON

class DocumentNoteController {
	static allowedMethods = [list:"POST", save:"POST"]

	def handlerChain

	def download = {
		def document = Document.get(params.documentId)
		def documentNote = document?.notes?.find { it.id == params.long("documentNoteId") }
		if (!document || !documentNote) {
			response.status = 404
			return
		}

		cache neverExpires: true

		def (filename, is, contentType, length) = handlerChain.downloadNote(document:document, documentNote:documentNote)
		is.withStream {
			response.setContentType(contentType)
			response.setContentLength(length)
			response.setHeader("Content-Disposition", "attachment; filename=${filename}")
			response.getOutputStream() << is
		}
	}

	def list = {
		def document = Document.get(params.documentId)
		assert document

		def urls = [:]
		document.notes.each {
			urls[it.id] = g.createLink(action:"download", params:[documentId:document.id, documentNoteId:it.id])
		}

		render(urls as JSON)
	}

	def save = {
		def document = Document.get(params.documentId)
		assert document

		def notes = JSON.parse(params?.notes).findAll { it.value }

		handlerChain.saveNotes([document:document, notes:notes])

		render([status:'success'] as JSON)
	}
}
