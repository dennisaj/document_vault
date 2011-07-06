package us.paperlesstech.handlers

import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType

class PclHandlerService extends Handler {
	static handlerFor = [MimeType.PCL]
	static transactional = true
	def handlerChain

	DocumentData createPdf(DocumentData d) {
		assert d.mimeType == MimeType.PCL

		String pclFile = fileService.getAbsolutePath(d)
		File pdfFile = File.createTempFile("pcl2pdf", ".pdf")

		try {
			// TODO FirstPage is only 3 when the file has the logos appended to the head of the pcl
			def cmd = """/usr/local/bin/pcl6 -dSAFER -dNOPAUSE -dBATCH -dFirstPage=3 -sDEVICE=pdfwrite -sOutputFile=${pdfFile.getAbsolutePath()} ${pclFile}"""
			log.debug "PDF create - ${cmd}"
			def proc = cmd.execute()
			proc.waitFor()
			if (proc.exitValue()) {
				throw new RuntimeException("Unable to process file for document ${d.id} - PCL to PDF conversion failed")
			}

			DocumentData pdf = fileService.createDocumentData(mimeType: MimeType.PDF, file: pdfFile)

			return pdf
		} finally {
			pdfFile?.delete()
		}
	}

	@Override
	void importFile(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)

		d.addToFiles(data)
		DocumentData pdf = createPdf(data)
		d.addToFiles(pdf)
		handlerChain.importFile(document: d, documentData: pdf)

		assert d.files.size() == 2
		assert d.previewImages.size() == d.files.first().pages
	}

	String pclToString(DocumentData data, boolean skipPclHeader = true) {
		String text = fileService.getText(data)

		// The text starts after the first double blank line
		if (skipPclHeader) {
			def startOfText = text.indexOf("\n\n")
			if (startOfText < 0)
				startOfText = text.indexOf("\r\n\r\n")
			text = text.substring(startOfText)
		}

		return text
	}
}
