package us.paperlesstech

import grails.converters.JSON

class DocumentController {
	static allowedMethods = [saveApi: "POST"]

	def index = {
		def results = Document.listOrderByDateCreated(max:5)

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
		response.setContentLength(document.data.length)
		response.getOutputStream().write(document.data)
	}
}