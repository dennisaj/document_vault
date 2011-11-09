package us.paperlesstech

import grails.converters.JSON

class NoteController {
	static allowedMethods = [list:"POST", saveLines:"POST", saveText:"POST",]

	def handlerChain
	def notificationService

	def download = {
		def noteDataId = params.long('noteDataId')
		assert noteDataId != null

		def document = Document.get(params.long('documentId'))
		def note = document?.notes?.find { it.data?.id == noteDataId }
		if (!document || !note) {
			response.status = 404
			render text:'File not found'
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

		render([notes:document.notes*.asMap()] as JSON)
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

		render([notification:notificationService.success('document-vault.api.notes.saveLines.success')] as JSON)
	}

	def saveText = {
		def document = Document.get(params.long('documentId'))
		assert document

		def text = params.text?.trim()
		assert text

		def pageNumber = Math.max(params.int('pageNumber') ?: 0, 0)
		assert pageNumber <= document.files.first().pages

		def left = params.float('left') as int ?: 0
		def top = params.float('top') as int ?: 0

		handlerChain.saveNotes(document:document, notes:[[text:text, left:left, top:top, pageNumber:pageNumber]])
		document.save(flush:true)

		render ([notification:notificationService.success('document-vault.api.notes.saveNote.success')] as JSON)
	}
}
