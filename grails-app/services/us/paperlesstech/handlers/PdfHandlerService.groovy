package us.paperlesstech.handlers

import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType
import us.paperlesstech.PreviewImage
import us.paperlesstech.helpers.FileHelpers
import us.paperlesstech.helpers.ImageHelpers

class PdfHandlerService extends Handler {
	static final handlerFor = [MimeType.PDF]
	static final LINEBREAK = 'LB'
	static transactional = true
	def handlerChain

	@Override
	void generatePreview(Map input) {
		def d = getDocument(input)
		log.info "Generating previews for the PDF for document ${d}"
		def data = getDocumentData(input)

		String pdfPath = fileService.getAbsolutePath(data)
		String baseName = FileHelpers.chopExtension(File.createTempFile("pdf2png", ".png").getAbsolutePath(), ".png")

		PdfReader pdfReader
		try {
			def cmd = """/usr/local/bin/gs -sDEVICE=png16m -r300 -dNOPAUSE -dBATCH -dSAFER -sOutputFile=${baseName}-%d.png ${pdfPath}"""
			log.debug "PreviewImage create - ${cmd}"
			def proc = cmd.execute()
			proc.waitFor()
			if (proc.exitValue()) {
				throw new RuntimeException("""Unable to generate preview for document ${d.id} - PDF to PNG conversion failed""")
			}

			d.resetPreviewImages()
			pdfReader = new PdfReader(pdfPath)
			(1..d.files.first().pages).each { page ->
				File f = new File("${baseName}-${page}.png")
				assert f.canRead(), "Didn't generate page $page from the PDF for document $d"

				Rectangle psize = pdfReader.getPageSize(page)

				PreviewImage i = new PreviewImage(pageNumber: page, width: psize.getWidth(), height: psize.getHeight())
				def bytes = ImageHelpers.scaleImage(f.getBytes(), i.width * 2, i.height * 2)
				i.data = fileService.createDocumentData(mimeType: MimeType.PNG, bytes: bytes)
				d.addToPreviewImages(i)
			}

			assert d.previewImages.size() > 0
		} finally {
			pdfReader?.close()
			(1..d.files.first().pages).each { page ->
				File f = new File("${baseName}-${page}.png")
				if (f.exists()) {
					f.delete()
				}
			}
		}

		log.info "Images created for document ${d}"
	}

	@Override
	void importFile(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)

		PdfReader pdfReader
		try {
			pdfReader = new PdfReader(fileService.getAbsolutePath(data))
			data.pages = pdfReader.getNumberOfPages()
			d.addToFiles(data)
		} finally {
			pdfReader?.close()
		}

		handlerChain.generatePreview(input)

		assert d.files.size() >= 1
		assert d.previewImages.size() == d.files.first().pages
	}

	@Override
	void cursiveSign(Map input) {
		def d = getDocument(input)
		log.info "Signing the PDF for document ${d}"
		def data = getDocumentData(input)
		def signatures = input.signatures

		assert signatures, "This method requires a map of signatures"

		log.info "Signing the PDF for document ${d}"

		PdfReader pdfReader
		PdfStamper pdfStamper
		ByteArrayOutputStream output = new ByteArrayOutputStream()
		try {
			pdfReader = new PdfReader(fileService.getAbsolutePath(data))
			pdfStamper = new PdfStamper(pdfReader, output)

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
		} finally {
			pdfStamper?.close()
			pdfReader?.close()
		}

		DocumentData newPdf = fileService.createDocumentData(mimeType: data.mimeType, pages: data.pages, bytes: output.toByteArray())
		d.addToFiles(newPdf)
		input.documentData = newPdf

		handlerChain.generatePreview(input)
	}
}
