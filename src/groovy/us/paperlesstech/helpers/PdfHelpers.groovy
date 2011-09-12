package us.paperlesstech.helpers

import java.io.File

import us.paperlesstech.Document
import us.paperlesstech.DocumentData

import com.lowagie.text.PageSize
import com.lowagie.text.Paragraph
import com.lowagie.text.Rectangle
import com.lowagie.text.pdf.BaseFont
import com.lowagie.text.pdf.ColumnText
import com.lowagie.text.pdf.PdfContentByte
import com.lowagie.text.pdf.PdfReader
import com.lowagie.text.pdf.PdfStamper

class PdfHelpers {
	public static File addNotesToPdf(Document d, DocumentData data, pdfPath) {
		PdfReader pdfReader
		PdfStamper pdfStamper
		ByteArrayOutputStream output = new ByteArrayOutputStream()
		def notesByPage = d.notes.groupBy { it.pageNumber }

		try {
			BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED)
			pdfReader = new PdfReader(pdfPath)
			pdfStamper = new PdfStamper(pdfReader, output)

			// Handle notes added to the zeroth page
			if (notesByPage[0]) {
				Rectangle size = PageSize.LETTER
				int lastPage = pdfReader.getNumberOfPages()
				pdfStamper.insertPage(++lastPage, size)
				ColumnText columnText = new ColumnText(pdfStamper.getOverContent(lastPage))
				columnText.setSimpleColumn(size.llx, size.lly, size.urx, size.ury)

				notesByPage[0].reverse().each {
					columnText.addElement(new Paragraph(it.note))
				}

				int status = columnText.go()
				while ((status & ColumnText.NO_MORE_TEXT) == 0) {
					pdfStamper.insertPage(++lastPage, size)
					columnText.setCanvas(pdfStamper.getOverContent(lastPage))
					columnText.setSimpleColumn(size.llx, size.lly, size.urx, size.ury)
					status = columnText.go()
				}
			}

			(1..data.pages).each { i ->
				def notes = notesByPage[i]
				if (!notes) {
					return
				}

				PdfContentByte content = pdfStamper.getOverContent(i)
				Rectangle psize = pdfReader.getPageSize(i)

				ColumnText columnText = new ColumnText(content)
				notes.each {
					columnText.setSimpleColumn(it.left as float, 0f, psize.width, (psize.height - it.top) as float)
					columnText.addText(new Paragraph(it.note))
					columnText.go()
				}
			}
		} finally {
			pdfStamper?.close()
			pdfReader?.close()
		}

		def f = File.createTempFile("printnotes-", ".pdf")
		f << output.toByteArray()

		f
	}
}
