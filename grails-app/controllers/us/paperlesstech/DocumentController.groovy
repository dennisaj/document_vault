package us.paperlesstech

import grails.converters.JSON

import org.compass.core.engine.SearchEngineQueryParseException

class DocumentController {
	static allowedMethods = [finalize: "GET", image: "GET", saveApi: "POST"]

	// TODO Remove scaffolding
	def scaffold = true

	def activityLogService
	def documentService
	def imageDataPrefix = "data:image/png;base64,"
	def searchableService

	def index = {
		def results = Document.listOrderByDateCreated(max:5, order:"desc")
		def docCount = Document.count()

		["searchResult":["results":results, "offset":0, "total":docCount, "max":docCount], "queryString":"5 most recent documents"]
	}

	def documentTypeOptions = {
		def dt = params.documentTypeId?.trim() ? DocumentType.get(params.documentTypeId) : null
		render(template:"documentTypeSearch", model:[documentType: dt])
	}

	def search = {
		def results = [:]

		// Default to simple search
		def simpleSearch = params?.simpleSearch ? params.simpleSearch.toBoolean() : true
		
		System.err.println("simple search '${simpleSearch}'")

		def queryString = ""
		if(!simpleSearch) {
			// Find the incoming fields that start with field_, strip off the field_
			// and create a query string from them
			def advancedQuery = params.findAll { it.key.startsWith("field_") && it.value }.collect { "${it.key[6..-1]}:*${it.value}*" }.join(" ")
			System.err.println("advancedQuery '${advancedQuery}'")
			def dt = DocumentType.get(params.documentType)
			// if there is a document type add it to the search
			queryString = dt ? "DocumentType:${dt.name} ${advancedQuery}" : advancedQuery
		} else {
			queryString = params.q
		}
		System.err.println("queryString '${queryString}'")

		queryString = queryString.trim()
		System.err.println("queryString '${queryString}'")
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

	def saveApi = {
		Document document = new Document()
		try {
			document.pcl = new Pcl(data:params.data)
			document.save(flush:true)
			response.status = 200
			render "Document saved\n"
			PclProcessorJob.triggerNow(documentId:document.id)
			log.info "Saved document ${document.id}"
		} catch(Exception e) {
			log.error("Unable to save uploaded document", e)
			response.status = 500
			render "Error saving file\n"
			document.delete()
		}
	}

	def downloadImage = {
		def document = Document.get(params.id)
		if(!document.images) {
			// TODO handle documents without images
			return
		}
		def pageNumber = params.pageNumber ?: 0
		def image = document.getSortedImages()[pageNumber]

		response.setContentType("image/png")
		response.setContentLength(image.data.length)
		response.getOutputStream().write(image.data)
	}

	def downloadPdf = {
		def document = Document.get(params.id)
		response.setContentType("application/pdf")
		response.setContentLength(document.pdf.data.length)
		response.getOutputStream().write(document.pdf.data)
	}

	def show = {
		def document = Document.get(params.id)
		activityLogService.addViewLog(request, document)

		render([view: "edit", model:[document: document]])
	}

	def edit = {
		def document = Document.get(params.id)
		activityLogService.addViewLog(request, document)

		[document: document]
	}

	def image = {
		def document = Document.get(params.id)

		if (document) {
			def image = document.getSortedImages()[params.pageNumber.toInteger()]
			if (image) {
				def j = [imageData: imageDataPrefix + image.data.encodeBase64().toString(),
							pageNumber: params.pageNumber,
							sourceHeight: image.sourceHeight,
							sourceWidth: image.sourceWidth]
				render j as JSON
			}
		}

		render [:] as JSON
	}

	def sign = {
		def document = Document.get(params.id)

		if (document && params.imageData) {
			if (!session.signatures) {
				session.signatures = [:]
			}

			def signatures = session.signatures.get(document.id.toString(), [:])

			def imageData = params.imageData.substring(imageDataPrefix.size()).decodeBase64()

			signatures[params.pageNumber] = imageData
		}

		render [:] as JSON
	}

	def finish = {
		def document = Document.get(params.id)
		if (document) {
			activityLogService.addSignLog(request, document, session.signatures.get(document.id.toString()).findAll {it.value})
			documentService.signDocument(document, session.signatures.get(document.id.toString()))

			document.signed = true
			document.save()

			flash.green = "Signature saved"
		}

		render [:] as JSON
	}
}
