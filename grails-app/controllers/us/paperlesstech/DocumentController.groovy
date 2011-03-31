package us.paperlesstech

import grails.converters.JSON

class DocumentController {
	static allowedMethods = [finalize: "GET", image: "GET", saveApi: "POST"]
	
	// TODO Remove scaffolding
	def scaffold = true
	
	def imageDataPrefix = "data:image/png;base64,"
	def documentService
	def activityLogService

	def index = {
		def results = Document.listOrderByDateCreated(max:5, order:"desc")

		[documents: results]
	}

	def search = {
		if(!params.term || params.term.length() < 3) {
			render(template:"searchResults")
			return
		}
		String roNumber = params.term.toLowerCase()
		def results = Document.executeQuery("from Document d where d.id like :roNumber", [roNumber: "%${roNumber}%"])

		render(template:"searchResults", model:[documents: results])
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

		render([]) as JSON
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

		render([]) as JSON
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

		render([]) as JSON
	}
}
