package us.paperlesstech

import grails.converters.JSON

class NoteController {
	static allowedMethods = [list:"POST", saveLines:"POST", saveTextNote:"POST",]

	def authService
	def handlerChain

	def download = {
		def document = Document.get(params.documentId)
		def note = document?.notes?.find { it.data?.id == params.long("noteDataId") }
		if (!document || !note?.data) {
			response.status = 404
			return
		}

		cache neverExpires: true

		def (filename, is, contentType, length) = handlerChain.downloadNote(document:document, note:note)
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

		def notes = [:]
		document.notes.each {
			if (it.data) {
				notes[it.id] = [url:g.createLink(action:"download", params:[documentId:document.id, noteDataId:it.data.id]), note:it.note]
			}
		}

		render(notes as JSON)
	}

	def saveLines = {
		def document = Document.get(params.documentId)
		assert document

		def notes = []
		JSON.parse(params?.notes).each {
			if (it) {
				it.value
				notes << [lines:it.value]
			}
		}

		handlerChain.saveNotes([document:document, notes:notes])

		render([status:'success'] as JSON)
	}

	def saveText = {
		def document = Document.get(params.long('documentId'))
		assert authService.canNotes(document)

		if (document) {
			def value = params.value?.trim()
			if (value) {
				handlerChain.saveNotes([document:document, notes:[[text:value]]])
				document.save(flush:true)
			}

			render template:"textNotes", model:[document:document]
			return
		}

		render ([status:"error"] as JSON)
	}
}
