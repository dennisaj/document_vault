package us.paperlesstech

import grails.converters.JSON

class DocumentController {
	static navigation = [[group:'tabs', action:'index', isVisible: { authService.isLoggedIn() }, order:0, title:'Search']]

	def authService
	def documentService
	def handlerChain

	def downloadImage = {
		def document = Document.get(params.long("documentId"))
		if (!document) {
			response.status = 404
			render text:'File not found'
			return
		}

		def (filename, is, contentType, length) = handlerChain.downloadPreview(document:document, page:params.int('pageNumber') ?: 1)
		is.withStream { stream->
			response.setContentType(contentType)
			response.setContentLength(length)
			response.getOutputStream() << stream
		}
	}

	def download = {
		def document = Document.get(params.long("documentId"))
		def documentData = document?.files?.find { it.id == params.long("documentDataId")}
		if (!document || !documentData) {
			response.status = 404
			render text:'File not found'
			return
		}

		cache neverExpires:true

		def (filename, is, contentType, length) = handlerChain.download(document:document, documentData:documentData)
		is.withStream {
			response.setContentType(contentType)
			response.setContentLength(length)
			response.setHeader("Content-Disposition", "attachment; filename=${filename}")
			response.getOutputStream() << is
		}
	}

	def thumbnail = {
		def document = Document.get(params.long("documentId"))
		def documentData = document?.previewImages?.find { it.thumbnail.id == params.long("documentDataId") }
		if (!document || !documentData) {
			response.status = 404
			render text:'File not found'
			return
		}

		cache neverExpires:true

		def (filename, is, contentType, length) = handlerChain.downloadThumbnail(document:document, page:params.int('pageNumber') ?: 1)
		is.withStream {
			response.setContentType(contentType)
			response.setContentLength(length)
			response.setHeader("Content-Disposition", "attachment; filename=${filename}")
			response.getOutputStream() << is
		}
	}

	def show = {
		def document = Document.get(params.long('documentId'))
		assert document

		def map = document.asMap()
		map.permissions = [:]
		map.permissions.sign = authService.canSign(document)
		map.permissions.getSigned = authService.canGetSigned(document)
		map.permissions.notes= authService.canNotes(document)
		map.permissions.print= authService.canPrint(document)
		// We cannot get to this action if this is false but we will include it for the sake of completeness.
		map.permissions.view = authService.canView(document)

		render([document:map] as JSON)
	}

	def image = {
		def d = Document.get(params.long("documentId"))
		def pageNumber = params.int("pageNumber")
		assert d

		def map = d.previewImageAsMap(pageNumber)
		map.highlights = (authService.canGetSigned(d) || authService.canSign(d) ? d.highlightsAsMap(pageNumber) : [:])

		render(map as JSON)
	}

	def list = {
		def searchFolder = Folder.load(params.long('folderId'))

		def pagination = [:]
		def max = params.int('max')
		pagination.max = max in 10..100 ? max : (max > 100 ? 100 : 10)
		pagination.sort = params.sort ?: 'dateCreated'
		pagination.order = params.order ?: 'asc'
		pagination.offset = params.int('offset') ?: 0

		def results = documentService.search(searchFolder, pagination, params.filter?.trim())

		render([searchFolder:searchFolder?.asMap(), documents:results.documentResults*.asMap(), total:results.documentTotal] as JSON)
	}
}
