package us.paperlesstech.handlers

import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType
import us.paperlesstech.helpers.FileHelpers

class PclHandlerService extends Handler {
	static handlerFor = [MimeType.PCL]
	static transactional = true
	def handlerChain

	DocumentData createPdf(DocumentData d) {
		assert d?.data

		File pclFile = File.createTempFile("vault", ".pcl")
		String baseName = FileHelpers.chopExtension(pclFile.getAbsolutePath(), ".pcl")
		File pdfFile = new File(baseName + ".pdf")

		try {
			pclFile.setBytes(d.data)

			// TODO FirstPage is only 3 when the file has the logos appended to the head of the pcl
			def cmd = """/usr/local/bin/pcl6 -dSAFER -dNOPAUSE -dBATCH -dFirstPage=3 -sDEVICE=pdfwrite -sOutputFile=${baseName}.pdf ${pclFile.getAbsolutePath()}"""
			log.debug "PDF create - ${cmd}"
			def proc = cmd.execute()
			proc.waitFor()
			if (proc.exitValue()) {
				throw new RuntimeException("Unable to process file for document ${d.id} - PCL to PDF conversion failed")
			}

			DocumentData pdf = new DocumentData(mimeType: MimeType.PDF)
			pdf.data = pdfFile.getBytes()

			return pdf
		} finally {
			pdfFile?.delete()
			pclFile?.delete()
		}
	}

	@Override
	void importFile(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)

		DocumentData pdf = createPdf(data)
		d.addToFiles(pdf)
		handlerChain.importFile(document: d, documentData: pdf)
	}

	static String pclToString(DocumentData data, boolean skipPclHeader = true) {
		String text = new ByteArrayInputStream(data.data).getText();

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
