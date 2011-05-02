package us.paperlesstech

import grails.converters.JSON

import org.compass.core.engine.SearchEngineQueryParseException
import us.paperlesstech.handlers.Handler

class DocumentController {
	static allowedMethods = [finalize: "GET", image: "GET", savePcl: "POST"]
	static navigation = [[action:'index', isVisible: {springSecurityService.isLoggedIn()}, order:0, title:'Home']]

	// TODO Remove scaffolding
	def scaffold = true

	def activityLogService
	Handler handlerChain
	def imageDataPrefix = "data:image/png;base64,"
	def searchableService
	def signatureCodeService
	def signatureService
	def springSecurityService

	def index = {
		def results = Document.listOrderByDateCreated(max:5, order:"desc")
		def docCount = Document.count()

		["searchResult":["results":results, "offset":0, "total":docCount, "max":docCount], "queryString":"5 most recent documents"]
	}

	def search = {
		def results = [:]

		// Default to simple search
		def simpleSearch = params?.simpleSearch ? params.simpleSearch.toBoolean() : true
		
		def queryString = ""
		if(!simpleSearch) {
			// Find the incoming fields that start with field_, strip off the field_
			// and create a query string from them
			def advancedQuery = params.findAll { it.key.startsWith("field_") && it.value }.collect { "${it.key[6..-1]}:*${it.value}*" }.join(" ")
			// TODO fix me
			def dt = null
			// if there is a document type add it to the search
			queryString = dt ? "DocumentType:${dt.name} ${advancedQuery}" : advancedQuery
		} else {
			queryString = params.q
		}

		queryString = queryString.trim()
		if (queryString) {
			try {
				def ss = searchableService.search(queryString, params)
				def docs = ss?.results.collect { Document.findWhere(text: it) }
				results = ["searchResult":["results":docs, "offset":ss.offset, "total":ss.total, "max":ss.max]]
			} catch (SearchEngineQueryParseException ex) {
				results = [parseException: true]
			}
		}

		results.queryString = queryString
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

	def savePcl = {
		try {
			Document document = new Document()
			def documentData = new DocumentData(mimeType: MimeType.PCL, data: params.data)
			handlerChain.importFile(document: document, documentData: documentData)

			assert document.files.size() == 1
			document.save()

			response.status = 200
			render "Document ${document.id} saved\n"
			log.info "Saved document ${document.id}"
		} catch (Exception e) {
			log.error("Unable to save uploaded document", e)
			response.status = 500
			render "Error saving file\n"
		}
	}

	def downloadImage = {
		def document = Document.get(params.id)
		if(!document.previewImages) {
			// TODO handle documents without images
			return
		}
		def pageNumber = params.pageNumber ?: 1
		def image = document.previewImage(pageNumber)

		response.setContentType("image/png")
		response.setContentLength(image.data.data.length)
		response.getOutputStream().write(image.data.data)
	}

	def downloadPdf = {
		def document = Document.get(params.id)
		if (document) {
			response.setContentType("application/pdf")
			response.setContentLength(document.files.first().data.length)
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

		render(d.previewImageAsMap(params.pageNumber.toInteger()) as JSON)
	}

	def sign = {
		if (!session.signatures) {
			session.signatures = [:]
		}
		
		def signatures = session.signatures.get(params.id.toString(), [:])
		
		if (signatureService.saveSignatureToMap(signatures, params.pageNumber, params.imageData)) {
			session.signatures[params.id] = signatures
			render ([status:"success"] as JSON)
		} else {
			render ([status:"error"] as JSON)
		}
	}

	def finish = {
		def document = Document.get(params.id)
		if (document && !document.signed) {
			def signatures = session.signatures.get(document.id.toString()).findAll {it.value}

			activityLogService.addSignLog(document, signatures)
			handlerChain.sign(document: document, documentData: document.files.first(), signatures)

			document.signed = true
			document.save()

			flash.green = "Signature saved"
			render ([status:"success"] as JSON)
		}

		render ([status:"error"] as JSON)
	}
}
