package us.paperlesstech

import grails.converters.JSON

class NoteController {
	static allowedMethods = [list:"POST", saveLines:"POST", saveText:"POST",]

	def handlerChain
	def notificationService

	def download(Long documentId, Long noteDataId) {
		assert noteDataId != null

		def document = Document.get(documentId)
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
			if (!request.getHeader("User-Agent")?.contains("iPad")) {
				response.setHeader("Content-Disposition", "attachment; filename=\"${filename}\"")
			}
			response.getOutputStream() << stream
		}
	}

	def list(Long documentId) {
		def document = Document.get(documentId)
		assert document

		render([notes:document.notes*.asMap()] as JSON)
	}

	def saveLines(Long documentId, String notes) {
		def inputNotes = JSON.parse(notes)
		assert inputNotes

		def document = Document.get(documentId)
		assert document

		def outputNotes = []
		inputNotes.each {
			if (it) {
				it.value
				outputNotes << [lines:it.value]
			}
		}

		handlerChain.saveNotes(document:document, notes:outputNotes)
		document.save(flush:true)

		render([notification:notificationService.success('document-vault.api.notes.saveLines.success')] as JSON)
	}

	def saveText(Long documentId, String text, Integer pageNumber, int left, int top) {
		def document = Document.get(documentId)
		assert document

		text = text?.trim()
		assert text

		pageNumber = Math.max(pageNumber ?: 0, 0)
		assert pageNumber <= document.files.first().pages

		handlerChain.saveNotes(document:document, notes:[[text:text, left:left, top:top, pageNumber:pageNumber]])
		document.save(flush:true)

		render ([notification:notificationService.success('document-vault.api.notes.saveNote.success')] as JSON)
	}
}
