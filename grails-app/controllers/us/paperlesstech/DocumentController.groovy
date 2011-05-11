package us.paperlesstech

import grails.converters.JSON

import org.compass.core.CompassQuery
import org.compass.core.engine.SearchEngineQueryParseException

import us.paperlesstech.handlers.HandlerChain

class DocumentController {
	static allowedMethods = [finalize: "GET", image: "GET", savePcl: "POST"]
	static navigation = [[action:'index', isVisible: {springSecurityService.isLoggedIn()}, order:0, title:'Home']]

	// TODO Remove scaffolding
	def scaffold = true

	def activityLogService
	def grailsApplication
	def handlerChain
	def searchableService
	def springSecurityService
	def tagService

	def index = {
		def results = Document.listOrderByDateCreated(max:10, order:"desc")
		def docCount = Document.count()

		def tagList =  tagService.getRecentTags()

		["tagSearchResults": tagList,
				"documents": new SearchResult(results: results, offset: 0, total: docCount,
						max: docCount),
				"queryString": "*"]
	}

	def search = {
		def results = [:]

		def q = params?.q?.trim()
		results.queryString = q

		if (searchTags) {
			def tagList = []
			def terms = q?.tokenize(",").collect { it.trim() }.findAll { it }
			if (terms?.size()) {
				tagList = tagService.tagSearch(terms)
			} else {
				tagList = tagService.getRecentTags()
			}

			results.tagSearchResults = tagList
		}

		q = q ? q.tokenize().collect { "*$it*" }.join(" ") : "*"

		try {
			if(searchDocuments) {
				def ss = Document.search(reload: true) {
					must(queryString(q, [useAndDefaultOperator: true]))
					sort(CompassQuery.SortImplicitType.SCORE)
					sort("dateCreated", CompassQuery.SortPropertyType.STRING, CompassQuery.SortDirection.REVERSE)
				}
				results.documents = new SearchResult(results: ss.results, offset: ss.offset, total: ss.total,
						max: ss.max)
			}
		} catch (SearchEngineQueryParseException ex) {
			results.parseException = true
		}

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

		if (!session.signatures) {
			session.signatures = [:]
		}

		def signatures = session.signatures.get(params.id, [:])
		signatures[params.pageNumber] = JSON.parse(params.lines)
		session.signatures[params.id] = signatures

		render ([status:"success"] as JSON)
	}

	def finish = {
		def document = Document.get(params.id)
		if (document && !document.signed()) {
			def signatures = session.signatures.get(document.id.toString()).findAll {it.value}

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
