package us.paperlesstech


class PclProcessorJob {
	// TODO currently I am setting this to a date that will never trigger and triggering
	// the job manually. In the future change this so that triggered jobs will cleanup
	// any documents where the processing didn't happen (because of server shutdown or the like)
	static triggers = {
		cron name: 'pclProcessor', cronExpression: "0 0 0 1 1 ? 2038" // Set to never fire
	}

	def execute(context) {
		def id = context.mergedJobDataMap.get('documentId')
		if(!id) {
			log.info "Called with no id.  Context = ${context}"
			return
		}
	
		def document = Document.get(id)
		if(!document) {
			log.error "No document with id - ${id}"
			return
		}
	}
	
	def convertToPdf(Document d) {
		File pclFile = File.createTempFile("vault", ".pcl")
		File pdfFile = new File(chopExtension(pclFile.name, ".pcl") + ".pdf")
		
		try {
			pclFile.setBytes(d.pcl.data)	
			
			// TODO FirstPage is only 3 when the file has the logos appended to the head of the pcl
			def proc = """pcl6 -dNOPAUSE -dFirstPage=3 -sDEVICE=pdfwrite -sOutputFile=${pdfFile.getAbsolutePath()} ${pclFile.getAbsolutePath()}""".execute()
			if(proc.exitValue()) {
				throw new RuntimeException("Unable to process file for document ${d.id} - PCL conversion failed")
			}
		} finally {
			if(pclFile) {
				pclFile.delete();
			}
			if(pdfFile) {
				pdfFile.delete();
			}
		}
	}
	
	def chopExtension(String fileName, String extension) {
		return fileName.substring(0, extension.length())
	}
}