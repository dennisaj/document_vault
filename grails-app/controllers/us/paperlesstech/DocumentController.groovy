package us.paperlesstech

import grails.converters.JSON

import org.apache.shiro.SecurityUtils

class DocumentController {
	static allowedMethods = [finalize: "GET", image: "GET", savePcl: "POST"]
	static navigation = [[action:'index', isVisible: {SecurityUtils.subject.isPermitted("document:*")}, order:0, title:'Home']]

	// TODO Remove scaffolding
	def scaffold = true

	def activityLogService
	def grailsApplication
	def handlerChain
	def searchableService
	def tagService

	def index = {
		def results = Document.listOrderByDateCreated(max:10, order:"desc")
		def docCount = Document.count()

		def tagList =  tagService.getRecentTags()

		["tagSearchResults": tagList, "documents": [offset: 0, results: Document.list([max:10, order:'desc', sort:'dateCreated']), total:10, max:10], "queryString": "*"]
	}

	def search = {
		def results = [:]

		def q = params?.q?.trim()
		results.queryString = q

		def tagList = []
		def terms = q?.tokenize(",").collect { it.trim() }.findAll { it }
		if (terms?.size()) {
			tagList = tagService.tagSearch(terms)
		} else {
			tagList = tagService.getRecentTags()
		}

		results.tagSearchResults = tagList

		def docs = Document.findAllByNameIlike("%$q%", [max:10, order:'desc', sort:'dateCreated'])
		results.documents = [results: docs, offset: 0, total:docs.size(), max:10]
		render(template:"searchResults", model:results)
	}

	def saveNote = {
		def idRegex = params.id =~ /^.+\-(\d+)$/

		if (idRegex.matches() && Document.exists(idRegex[0][1])) {
			def document = Document.get(idRegex[0][1])
			document.searchFields['Note'] = params.value
			document.save(flush:true)

			render text:params.value, contentType: "text/plain"
			return
		}

		render ([status:"error"] as JSON)
	}

	def downloadImage = {
		def document = Document.get(params.id)
		if (document) {
			def (filename, data, contentType) = handlerChain.retrievePreview(document: document, page: params.pageNumber?.toInteger() ?: 1)
			response.setContentType(contentType)
			response.setContentLength(data.length)
			response.getOutputStream().write(data)
		}

		response.status = 404
	}

	def download = {
		def document = Document.get(params.id)
		if (document) {
			def (filename, data, contentType) = handlerChain.download(document: document, documentData: document.files.first())
			response.setContentType(contentType)
			response.setContentLength(data.length)
			response.setHeader("Content-Disposition", "attachment; filename=${filename}")
			response.getOutputStream().write(data)
		}

		response.status = 404
	}

	def show = {
		def document = Document.get(params.id)
		activityLogService.addViewLog(document)

		render([view: "edit", model:[document: document]])
	}

	def edit = {
		def document = Document.get(params.id)
		activityLogService.addViewLog(document)

		[document: document]
	}

	def image = {
		def d = Document.get(params.id.toInteger())
		assert d

		def map = d.previewImageAsMap(params.pageNumber.toInteger())
		render(map as JSON)
	}

	def sign = {
		assert params.lines
		def document = Document.get(params.id)
		if (document && !document.signed()) {
			def signatures = JSON.parse(params.lines).findAll {it.value}

			if (signatures) {
				activityLogService.addSignLog(document, signatures)
				handlerChain.sign(document: document, documentData: document.files.first(), signatures:signatures)

				document.save()
			}

			flash.green = "Signature saved"
			render ([status:"success"] as JSON)
		}

		render ([status:"error"] as JSON)
	}
}
