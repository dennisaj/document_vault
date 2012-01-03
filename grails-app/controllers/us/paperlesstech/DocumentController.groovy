package us.paperlesstech

import grails.converters.JSON

class DocumentController {
	def authService
	def documentService
	def handlerChain
	def notificationService
	def tenantService

	def downloadImage = {
		def document = Document.get(params.long("documentId"))
		def previewImage = document?.previewImages?.find { it.data.id == params.long("documentDataId") }
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
			response.contentType = contentType
			response.contentLength = length
			if (!request.getHeader("User-Agent")?.contains("iPad")) {
				response.setHeader("Content-Disposition", "attachment; filename=\"${filename}\"")
			}
			response.outputStream << is
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
			response.contentType = contentType
			response.contentLength = length
			if (!request.getHeader("User-Agent")?.contains("iPad")) {
				response.setHeader("Content-Disposition", "attachment; filename=\"${filename}\"")
			}
			response.outputStream << is
		}
	}

	def show = {
		def document = Document.get(params.long('documentId'))
		assert document

		def map = document.asMap()
		
		def party = Party.findByDocumentAndSignator(document, authService.authenticatedUser)
		def colors = party?.color ? [party?.color?.name()] : PartyColor.values()*.name()

		def pages = (1..map.data.pages).collect { pageNumber->
			def image = document.previewImageAsMap(pageNumber)
			image.savedHighlights = (map.permissions.getSigned || map.permissions.sign ? document.highlightsAsMap(pageNumber) : [:])
			image
		}

		if (!map.permissions.notes) {
			map.notes = []
		}

		render([document:map, pages:pages, colors:colors] as JSON)
	}

	def image = {
		def d = Document.get(params.long("documentId"))
		def pageNumber = params.int("pageNumber")
		assert d

		def map = d.previewImageAsMap(pageNumber)
		map.savedHighlights = (authService.canGetSigned(d) || authService.canSign(d) ? d.highlightsAsMap(pageNumber) : [:])

		render(map as JSON)
	}

	def list = {
		def searchFolder = Folder.load(params.long('folderId'))

		def pagination = [:]
		def max = params.int('max')
		pagination.max = max in 10..100 ? max : (max > 100 ? 100 : 10)
		pagination.sort = params.sort ?: 'dateCreated'
		pagination.order = params.order ?: 'desc'
		pagination.offset = params.int('offset') ?: 0

		def results = documentService.filter(searchFolder, pagination, params.filter?.trim())

		render([searchFolder:searchFolder?.asMap(), documents:results.results*.asMap(), documentTotal:results.total] as JSON)
	}

	def search = {
		def pagination = [:]
		def max = params.int('max')
		pagination.max = max in 10..100 ? max : (max > 100 ? 100 : 10)
		pagination.sort = params.sort ?: 'dateCreated'
		pagination.order = params.order ?: 'asc'
		pagination.offset = params.int('offset') ?: 0

		def results = documentService.search(pagination, params.filter?.trim())

		render([documents:results.results*.asMap(), documentTotal:results.total] as JSON)
	}

	def flag = {
		def document = Document.get(params.long('documentId'))
		assert document
		def flag = params.flag
		assert flag && tenantService.getTenantConfigList('flag').contains(flag)

		assert authService.canFlag(document)

		document.addTag(flag)
		document.save()

		def returnMap = [:]
		returnMap.notification = notificationService.success('document-vault.api.document.flag.success', [document.name, flag])
		returnMap.document = document.asMap()

		render(returnMap as JSON)
	}

	def unflag = {
		def document = Document.get(params.long('documentId'))
		assert document
		def flag = params.flag
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
