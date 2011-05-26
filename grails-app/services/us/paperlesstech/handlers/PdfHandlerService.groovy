package us.paperlesstech.handlers

import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType
import us.paperlesstech.PreviewImage
import us.paperlesstech.helpers.FileHelpers
import us.paperlesstech.helpers.ImageHelpers

import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper

class PdfHandlerService extends Handler {
	static final handlerFor = [MimeType.PDF]
	static final LINEBREAK = 'LB'
	static transactional = true
	def handlerChain
	def nextService

	@Override
	void generatePreview(Map input) {
		def d = getDocument(input)
		log.info "Generating previews for the PDF for document ${d}"
		def data = getDocumentData(input)

		List filesToDelete = []

		File pdfFile = File.createTempFile("vault", ".pdf")
		filesToDelete += pdfFile

		String baseName = FileHelpers.chopExtension(pdfFile.getAbsolutePath(), ".pdf")

		try {
			pdfFile.setBytes(data.data)

			def cmd = """/usr/local/bin/gs -sDEVICE=png16m -r300 -dNOPAUSE -dBATCH -dSAFER -sOutputFile=${baseName}-%d.png ${pdfFile.getAbsolutePath()}"""
			log.debug "PreviewImage create - ${cmd}"
			def proc = cmd.execute()
			proc.waitFor()
			if (proc.exitValue()) {
				throw new RuntimeException("""Unable to generate preview for document ${d.id} - PDF to PNG conversion failed""")
			}

			DocumentData pdf = d?.files?.first()
			assert pdf?.data

			d.resetPreviewImages()
			PdfReader pdfReader = new PdfReader(pdf.data)
			for (int page = 1; page <= pdf.pages; page++) {
				File f = new File("${baseName}-${page}.png")
				assert f.canRead(), "Didn't generate page $page from the PDF for document $d"
				filesToDelete += f

				Rectangle psize = pdfReader.getPageSize(page)

				PreviewImage i = new PreviewImage(pageNumber: page, width: psize.getWidth(), height: psize.getHeight())
				def bytes = ImageHelpers.scaleImage(f.getBytes(), i.width * 2, i.height * 2)
				i.data = new DocumentData(mimeType: MimeType.PNG, data: bytes)
				d.addToPreviewImages(i)
			}

			pdfReader.close();

			assert d.previewImages.size() > 0
		} finally {
			filesToDelete.each { it.delete() }
		}

		log.info "Images created for document ${d}"
	}

	@Override
	void importFile(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)

		PdfReader pdfReader = new PdfReader(data.data)
		data.pages = pdfReader.getNumberOfPages()
		pdfReader.close()
		d.addToFiles(data)

		handlerChain.generatePreview(input)
	}

	@Override
	void sign(Map input) {
		def d = getDocument(input)
		log.info "Signing the PDF for document ${d}"
		def data = getDocumentData(input)
		def signatures = input.signatures

		assert signatures, "This method requires a map of signatures"

		log.info "Signing the PDF for document ${d}"

		PdfReader pdfReader = new PdfReader(data.data)

		ByteArrayOutputStream output = new ByteArrayOutputStream()
		PdfStamper pdfStamper = new PdfStamper(pdfReader, output)

		(1..data.pages).each {i ->
			def lines = signatures[i.toString()]
			if (!lines) {
				return
			}

			PdfContentByte content = pdfStamper.getOverContent(i)
			Rectangle psize = pdfReader.getPageSize(i)

			content.setLineWidth(0.05f)
			lines.each {
				if (it != LINEBREAK) {
					content.moveTo(it.a.x as float, (psize.height - it.a.y) as float)
					content.lineTo(it.b.x as float, (psize.height - it.b.y) as float)
					content.stroke()
				}
			}
		}

		pdfStamper.close()

		DocumentData newPdf = new DocumentData(mimeType: data.mimeType, pages: data.pages)
		newPdf.data = output.toByteArray()
		d.addToFiles(newPdf)
		d.signed = true
		input.documentData = newPdf

		handlerChain.generatePreview(input)
	}
}
