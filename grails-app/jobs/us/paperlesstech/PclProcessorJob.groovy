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

		def d = Document.get(id)
		if(!d) {
			log.error "No document - ${id}"
			return
		}

		log.info "Converting document - ${id}"
		createBinaries(d)
		d.save(flush:true)
	}

	def createBinaries(Document d) {
		assert d != null
		assert d.pcl != null
		assert d.pcl.data != null

		List filesToDelete = []

		File pclFile = File.createTempFile("vault", ".pcl")
		filesToDelete += pclFile

		String baseName = chopExtension(pclFile.getAbsolutePath(), ".pcl")

		try {
			pclFile.setBytes(d.pcl.data)

			// TODO FirstPage is only 3 when the file has the logos appended to the head of the pcl
			def cmd = """/usr/local/bin/pcl6 -dNOPAUSE -dFirstPage=3 -sDEVICE=pdfwrite -sOutputFile=${baseName}.pdf ${pclFile.getAbsolutePath()}"""
			log.debug "PDF create - ${cmd}"
			def proc = cmd.execute()
			proc.waitFor()
			if(proc.exitValue()) {
				throw new RuntimeException("Unable to process file for document ${d.id} - PCL to PDF conversion failed")
			}

			Pdf pdf = new Pdf()
			File f = new File(baseName + ".pdf")
			filesToDelete += f
			pdf.data = f.getBytes()
			d.pdf = pdf

			// TODO Same comment about FirstPage as above
			// -r150 creates a file with 150dpi, we find this readable even while zoomed on iPhone but it can be changed
			cmd = """/usr/local/bin/pcl6 -dNOPAUSE -dFirstPage=3 -sDEVICE=pngmono -r150 -sOutputFile=${baseName}-%d.png ${pclFile.getAbsolutePath()}"""
			log.debug "Image create - ${cmd}"
			proc = cmd.execute()
			proc.waitFor()
			if(proc.exitValue()) {
				throw new RuntimeException("Unable to process file for document ${d.id} - PCL to PNG conversion failed")
			}

			for(int page = 1; ; page++) {
				f = new File("${baseName}-${page}.png")
				if(!f.exists() || !f.canRead()) {
					break;
				}
				filesToDelete += f
				Image i = new Image(pageNumber:page)
				i.data = f.getBytes()
				d.addToImages(i)
			}

			assert d.images.size() > 0
		} finally {
			filesToDelete.each { it.delete() }
		}
	}

	def chopExtension(String fileName, String extension) {
		return fileName.substring(0, fileName.length() - extension.length())
	}
}