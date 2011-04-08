package us.paperlesstech

class PclProcessorJob {
	// TODO currently I am setting this to a date that will never trigger and triggering
	// the job manually. In the future change this so that triggered jobs will cleanup
	// any documents where the processing didn't happen (because of server shutdown or the like)
	static triggers = {
		cron name: 'pclProcessor', cronExpression: "0 0 0 1 1 ? 2038" // Set to never fire
	}
	def documentService

	def execute(context) {
		def id = context.mergedJobDataMap.get('documentId')
		if(!id) {
			log.info "Called with no id.  Context = ${context}"
			return
		}

		def d = Document.get(id)
		if(!d) {
			log.error "No document - ${id}"
			return
		}

		log.info "Converting document - ${id}"
		documentService.createPdfFromPcl(d)
		documentService.createImagesFromPcl(d)
		documentService.createTextFromPcl(d)
		d.save(flush:true)
		log.info "Finished converting document - ${id}"
	}
}