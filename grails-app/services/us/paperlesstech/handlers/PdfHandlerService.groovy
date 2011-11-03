package us.paperlesstech.handlers

import java.awt.Color

import org.springframework.core.io.ClassPathResource

import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType
import us.paperlesstech.PreviewImage
import us.paperlesstech.Printer
import us.paperlesstech.helpers.FileHelpers
import us.paperlesstech.helpers.PdfHelpers

import com.lowagie.text.Rectangle
import com.lowagie.text.pdf.PdfContentByte
import com.lowagie.text.pdf.PdfReader
import com.lowagie.text.pdf.PdfStamper

class PdfHandlerService extends Handler {
	static final handlerFor = [MimeType.PDF]
	static final LINEBREAK = 'LB'
	static pdf2png = new ClassPathResource("scripts/pdf2png.sh").file.absolutePath
	static pdfPrint = new ClassPathResource("scripts/pdf_print.sh").file.absolutePath
	static transactional = true

	def handlerChain

	@Override
	void generatePreview(Map input) {
		def d = getDocument(input)
		log.info "Generating previews for the PDF for document ${d}"
		def data = getDocumentData(input)

		String pdfPath = fileService.getAbsolutePath(data)
		String baseName = FileHelpers.chopExtension(pdfPath, ".pdf")

		PdfReader pdfReader
		try {
			def cmd = """/bin/bash $pdf2png $pdfPath"""
			log.debug "PreviewImage create - $cmd"
			def proc = cmd.execute()
			proc.waitFor()
			if (proc.exitValue()) {
				log.error proc.getErrorStream().text
				throw new RuntimeException("""Unable to generate preview for document ${d.id} - PDF to PNG conversion failed. Command: '$cmd'""")
			}

			d.resetPreviewImages()
			pdfReader = new PdfReader(pdfPath)
			(1..d.files.first().pages).each { page ->
				File f = new File("${baseName}-${page}.png")
				File thumbnail = new File("${baseName}-${page}-thumbnail.png")
				assert f.canRead(), "Didn't generate page $page from the PDF for document $d"
				assert thumbnail.canRead(), "Didn't generate thumbnail for page $page from the PDF for document $d"

				Rectangle psize = pdfReader.getPageSize(page)

				PreviewImage i = new PreviewImage(pageNumber: page, sourceWidth: psize.getWidth(), sourceHeight: psize.getHeight())
				i.data = fileService.createDocumentData(mimeType: MimeType.PNG, bytes: f.bytes)
				i.thumbnail = fileService.createDocumentData(mimeType:MimeType.PNG, bytes:thumbnail.bytes)
				d.addToPreviewImages(i)
			}

			assert d.previewImages.size() > 0
		} finally {
			pdfReader?.close()

			File basefile = new File("${baseName}.png")
			if (basefile.exists()) {
				basefile.delete()
			}

			(1..d.files.first().pages).each { page ->
				File f = new File("${baseName}-${page}.png")
				File thumbnail = new File("${baseName}-${page}-thumbnail.png")
				if (f.exists()) {
					f.delete()
				}

				if (thumbnail.exists()) {
					thumbnail.delete()
				}
			}
		}

		log.info "Images created for document ${d}"
	}

	@Override
	void importFile(Map input) {
		def d = getDocument(input)

		int pages
		PdfReader pdfReader
		try {
			pdfReader = new PdfReader(input.bytes)
			pages = pdfReader.getNumberOfPages()
		} finally {
			pdfReader?.close()
		}

		def data = fileService.createDocumentData(mimeType: MimeType.PDF, bytes: input.bytes, pages: pages)
		d.addToFiles(data)
		input.bytes = null

		handlerChain.generatePreview(document: d, documentData: data)

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

			(1..data.pages).each { i ->
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

	@Override
	boolean print(Map input) {
		def d = getDocument(input)
		Printer printer = input.printer
		log.info "Printing document ${d} to printer ${printer}"

		def data = d.files.first()
		def pdfPath = fileService.getAbsolutePath(data)

		if (input.addNotes) {
			def f = PdfHelpers.addNotesToPdf(d, data, pdfPath)
			pdfPath = f.getAbsolutePath()
		}

		try {
			def cmd = """/bin/bash $pdfPrint $pdfPath ${printer.host} ${printer.port} ${printer.deviceType}"""
			log.debug "PDF print - $cmd"
			def proc = cmd.execute()
			proc.waitFor()
			if (proc.exitValue()) {
				log.error proc.getErrorStream().text
				log.error "Unable to print document ${d.id} - PDF print failed. Command: '$cmd'"

				return false
			}

			return true
		} finally {
			if (input.addNotes) {
				new File(pdfPath).delete()
			}
		}
	}
}
