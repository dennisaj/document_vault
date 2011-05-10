package us.paperlesstech

import grails.converters.JSON

import org.compass.core.engine.SearchEngineQueryParseException

import us.paperlesstech.handlers.Handler
import org.grails.taggable.Tag
import org.compass.core.CompassQuery

class DocumentController {
	static allowedMethods = [finalize: "GET", image: "GET", savePcl: "POST"]
	static navigation = [[action:'index', isVisible: {springSecurityService.isLoggedIn()}, order:0, title:'Home']]

	// TODO Remove scaffolding
	def scaffold = true

	def activityLogService
	def grailsApplication
	Handler handlerChain
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
		if(!document.previewImages) {
			// TODO handle documents without images
			return
		}
		def pageNumber = params.pageNumber?.toInteger() ?: 1
		def image = document.previewImage(pageNumber)

		response.setContentType(image.data.mimeType.downloadContentType)
		response.setContentLength(image.data.data.length)
		response.getOutputStream().write(image.data.data)
	}

	def download = {
		def document = Document.get(params.id)
		if (document) {
			def filename = document.toString() + document.files.first().mimeType.getDownloadExtension()
			response.setContentType(document.files.first().mimeType.downloadContentType)
			response.setContentLength(document.files.first().data.length)
			response.setHeader("Content-Disposition", "attachment; filename=${filename}")
			response.getOutputStream().write(document.files.first().data)
		}
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
