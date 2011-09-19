package us.paperlesstech

import grails.converters.JSON

class NoteController {
	static allowedMethods = [list:"POST", saveLines:"POST", saveText:"POST",]

	def handlerChain

	def download = {
		def noteDataId = params.long('noteDataId')
		assert noteDataId != null

		def document = Document.get(params.long('documentId'))
		def note = document?.notes?.find { it.data?.id == noteDataId }
		if (!document || !note) {
			response.status = 404
			return
		}

		cache neverExpires: true

		def (filename, is, contentType, length) = handlerChain.downloadNote(document:document, note:note)
		is.withStream { stream->
			response.setContentType(contentType)
			response.setContentLength(length)
			response.setHeader("Content-Disposition", "attachment; filename=${filename}")
			response.getOutputStream() << stream
		}
	}

	def list = {
		def document = Document.get(params.documentId)
		assert document

		def notes = [:]
		document.notes.each {
			def url = (it.data ? g.createLink(action:"download", params:[documentId:document.id, noteDataId:it.data.id]) : "")
			notes[it.id] = [url:url, note:it.note, pageNumber:it.pageNumber, left:it.left, top:it.top]
		}

		render(notes as JSON)
	}

	def saveLines = {
		def inputNotes = JSON.parse(params.notes)
		assert inputNotes

		def document = Document.get(params.documentId)
		assert document

		def notes = []
		inputNotes.each {
			if (it) {
				it.value
				notes << [lines:it.value]
			}
		}

		handlerChain.saveNotes(document:document, notes:notes)
		document.save(flush:true)

		render([status:'success'] as JSON)
	}

	def saveText = {
		def document = Document.get(params.long('documentId'))
		assert document

		def value = params.value?.trim()
		assert value

		def pageNumber = Math.max(params.int('pageNumber') ?: 0, 0)
		assert pageNumber <= document.files.first().pages

		def left = params.float('left') as int ?: 0
		def top = params.float('top') as int ?: 0

		handlerChain.saveNotes(document:document, notes:[[text:value, left:left, top:top, pageNumber:pageNumber]])
		document.save(flush:true)

		render template:"textNotes", model:[document:document]
	}
}
