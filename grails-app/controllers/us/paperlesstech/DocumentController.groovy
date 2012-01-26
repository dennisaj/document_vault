package us.paperlesstech

import grails.converters.JSON

class DocumentController {
	def authService
	def documentService
	def handlerChain
	def notificationService
	def tenantService

	def downloadImage(Long documentId, Long documentDataId) {
		def document = Document.get(documentId)
		def previewImage = document?.previewImages?.find { it.data.id == documentDataId }
		if (!document || !previewImage) {
			response.status = 404
			render text:'File not found'
			return
		}

		cache neverExpires:true

		def (filename, is, contentType, length) = handlerChain.downloadPreview(document:document, previewImage:previewImage)
		is.withStream {
			response.contentType = contentType
			response.contentLength = length
			response.outputStream << is
		}
	}

	def download(Long documentId, Long documentDataId) {
		def document = Document.get(documentId)
		def documentData = document?.files?.find { it.id == documentDataId }
		if (!document || !documentData) {
			response.status = 404
			render text:'File not found'
			return
		}

		cache neverExpires:true

		def (filename, is, contentType, length) = handlerChain.download(document:document, documentData:documentData)
		is.withStream {
			response.contentType = contentType
			response.contentLength = length
			if (!request.getHeader("User-Agent")?.contains("iPad")) {
				response.setHeader("Content-Disposition", "attachment; filename=\"${filename}\"")
			}
			response.outputStream << is
		}
	}

	def thumbnail(Long documentId, Long documentDataId, Integer pageNumber) {
		def document = Document.get(documentId)
		def documentData = document?.previewImages?.find { it.thumbnail.id == documentDataId }
		if (!document || !documentData) {
			response.status = 404
			render text:'File not found'
			return
		}

		cache neverExpires:true

		def (filename, is, contentType, length) = handlerChain.downloadThumbnail(document:document, page:pageNumber ?: 1)
		is.withStream {
			response.contentType = contentType
			response.contentLength = length
			if (!request.getHeader("User-Agent")?.contains("iPad")) {
				response.setHeader("Content-Disposition", "attachment; filename=\"${filename}\"")
			}
			response.outputStream << is
		}
	}

	def show(Long documentId) {
		def document = Document.get(documentId)
		assert document

		def map = document.asMap()

		def party = Party.findByDocumentAndSignator(document, authService.authenticatedUser)
		def parties = map.permissions.getSigned ? document.parties*.asMap() : []
		def partyColors = PartyColor.values()*.name()
		def signingColors = party?.color ? [party?.color?.name()] : partyColors
		def highlights = party?.highlightsMappedByPage() ?: [:]

		def pages = (1..map.data.pages).collect { pageNumber->
			def page = document.previewImageAsMap(pageNumber)
			page.highlights = highlights[pageNumber] ?: []
			page
		}

		if (!map.permissions.notes) {
			map.notes = []
		}

		render([document:map, parties:parties, pages:pages, partyColors:partyColors, signingColors:signingColors] as JSON)
	}

	def list(Long folderId, String filter, Integer max, String sort, String order, Integer offset) {
		def searchFolder = Folder.load(folderId)

		def pagination = [:]
		pagination.max = max in 10..100 ? max : (max > 100 ? 100 : 10)
		pagination.sort = sort ?: 'dateCreated'
		pagination.order = order ?: 'desc'
		pagination.offset = offset ?: 0

		def results = documentService.filter(searchFolder, pagination, filter?.trim())

		render([searchFolder:searchFolder?.asMap(), documents:results.results*.asMap(), documentTotal:results.total] as JSON)
	}

	def search(String filter, Integer max, String sort, String order, Integer offset) {
		def pagination = [:]
		pagination.max = max in 10..100 ? max : (max > 100 ? 100 : 10)
		pagination.sort = sort ?: 'dateCreated'
		pagination.order = order ?: 'asc'
		pagination.offset = offset ?: 0

		def results = documentService.search(pagination, filter?.trim())

		render([documents:results.results*.asMap(), documentTotal:results.total] as JSON)
	}

	def flag(Long documentId, String flag) {
		def document = Document.get(documentId)
		assert document
		assert flag && tenantService.getTenantConfigList('flag').contains(flag)

		assert authService.canFlag(document)

		document.addTag(flag)
		document.save()

		def returnMap = [:]
		returnMap.notification = notificationService.success('document-vault.api.document.flag.success', [document.name, flag])
		returnMap.document = document.asMap()

		render(returnMap as JSON)
	}

	def unflag(Long documentId, String flag) {
		def document = Document.get(documentId)
		assert document
		assert flag && tenantService.getTenantConfigList('flag').contains(flag)

		assert authService.canFlag(document)

		if(document.tags.contains(flag)) {
			document.removeTag(flag)
			document.save()
		}

		def returnMap = [:]
		returnMap.notification = notificationService.success('document-vault.api.document.unflag.success', [document.name, flag])
		returnMap.document = document.asMap()

		render(returnMap as JSON)
	}
}
