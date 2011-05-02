package us.paperlesstech.handlers

import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfReader
import us.paperlesstech.Document
import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType
import us.paperlesstech.PreviewImage
import us.paperlesstech.helpers.FileHelpers

class PclHandlerService extends Handler {
	static handlerFor = MimeType.PCL
	static transactional = true
	Handler handlerChain
	def nextService

	DocumentData createPdf(DocumentData d) {
		assert d?.data

		File pclFile = File.createTempFile("vault", ".pcl")
		String baseName = FileHelpers.chopExtension(pclFile.getAbsolutePath(), ".pcl")
		File pdfFile = new File(baseName + ".pdf")

		try {
			pclFile.setBytes(d.data)

			// TODO FirstPage is only 3 when the file has the logos appended to the head of the pcl
			def cmd = """/usr/local/bin/pcl6 -dNOPAUSE -dBATCH -dFirstPage=3 -sDEVICE=pdfwrite -sOutputFile=${baseName}.pdf ${pclFile.getAbsolutePath()}"""
			log.debug "PDF create - ${cmd}"
			def proc = cmd.execute()
			proc.waitFor()
			if (proc.exitValue()) {
				throw new RuntimeException("Unable to process file for document ${d.id} - PCL to PDF conversion failed")
			}

			DocumentData pdf = new DocumentData(mimeType: MimeType.PDF)
			pdf.data = pdfFile.getBytes()

			PdfReader pdfReader = new PdfReader(pdf.data)
			pdf.pages = pdfReader.getNumberOfPages()
			pdfReader.close();

			return pdf
		} finally {
			if (pdfFile) {
				pdfFile.delete()
			}

			if (pclFile) {
				pclFile.delete()
			}
		}
	}

	@Override
	@InterceptHandler
	void importFile(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)

		DocumentData pdf = createPdf(data)
		d.addToFiles(pdf)
		handlerChain.generatePreview(input)
	}

	@Override
	@InterceptHandler
	void generatePreview(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)

		List filesToDelete = []

		File pclFile = File.createTempFile("vault", ".pcl")
		filesToDelete += pclFile

		String baseName = FileHelpers.chopExtension(pclFile.getAbsolutePath(), ".pcl")

		try {
			pclFile.setBytes(data.data)

			// -r150 creates a file with 150dpi, we find this readable even while zoomed on iPhone but it can be changed
			def cmd = """/usr/local/bin/pcl6 -dNOPAUSE -dBATCH -dFirstPage=3 -sDEVICE=pngmono -r150 -sOutputFile=${baseName}-%d.png ${pclFile.getAbsolutePath()}"""
			log.debug "PreviewImage create - ${cmd}"
			def proc = cmd.execute()
			proc.waitFor()
			if (proc.exitValue()) {
				throw new RuntimeException("Unable to process file for document ${d.id} - PCL to PNG conversion failed")
			}

			DocumentData pdf = d?.files?.first()
			assert pdf?.data

			PdfReader pdfReader = new PdfReader(pdf.data)
			for (int page = 1; page <= pdf.pages; page++) {
				File f = new File("${baseName}-${page}.png")
				if (!f.exists() || !f.canRead()) {
					break;
				}
				filesToDelete += f

				Rectangle psize = pdfReader.getPageSize(page);

				PreviewImage i = new PreviewImage(pageNumber: page, width: psize.getWidth(), height: psize.getHeight())
				i.data = new DocumentData(mimeType: MimeType.PNG, data: f.getBytes())
				d.addToPreviewImages(i)
			}

			pdfReader.close();

			assert d.previewImages.size() > 0
		} finally {
			filesToDelete.each { it.delete() }
		}

		log.info "Images created for document ${d}"
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

	@Override
	@InterceptHandler
	void print(Map input) {
	}

	@Override
	@InterceptHandler
	void sign(Map input) {
	}
}
