package us.paperlesstech

import grails.converters.JSON

class DocumentController {
	static allowedMethods = [saveApi: "POST"]

	def index = {}

	def search = {
		if(!params.term || params.term.length() < 3) {
			render(template:"searchResults")
			return
		}
		String roNumber = params.term.toLowerCase()
		def results = SimpleDocument.executeQuery("from SimpleDocument d where lower(d.roNumber) like :roNumber", [roNumber: "%${roNumber}%"])

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
		} catch(Exception e) {
			log.error("Unable to save uploaded document", e)
			response.status = 500
			render "Error saving file\n"
			document.delete()
		}
	}

	def downloadPdf = {
		def document = Document.get(params.id)
		response.setContentType("application/pdf")
		response.setContentLength(document.data.length)
		response.getOutputStream().write(document.data)
	}
}