package us.paperlesstech

import grails.converters.JSON

class NoteController {
	static allowedMethods = [list:"POST", saveLines:"POST", saveText:"POST",]

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
			def url = (it.data ? g.createLink(action:"download", params:[documentId:document.id, noteDataId:it.data.id]) : "")
			notes[it.id] = [url:url, note:it.note, page:it.page, left:it.left, top:it.top]
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
		document.save(flush:true)

		render([status:'success'] as JSON)
	}

	def saveText = {
		def document = Document.get(params.long('documentId'))
		assert authService.canNotes(document)

		if (document) {
			def value = params.value?.trim()
			def page = params.int('page') ?: 0
			def left = params.float('left') as int ?: 0
			def top = params.float('top') as int ?: 0

			assert page <= document.files.first().pages

			if (value) {
				handlerChain.saveNotes([document:document, notes:[[text:value, left:left, top:top, page:page]]])
				document.save(flush:true)
			}

			render template:"textNotes", model:[document:document]
			return
		}

		render ([status:"error"] as JSON)
	}
}
