package us.paperlesstech.handlers

import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType

import com.itextpdf.text.Image
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper

class PdfHandlerService extends Handler {
	static handlerFor = MimeType.PDF
	static transactional = true
	Handler handlerChain
	def nextService

	@Override
	@InterceptHandler
	void sign(Map inputs) {
		def document = getDocument(inputs)
		log.info "Signing the PDF for document ${document}"
		def data = getDocumentData(inputs)
		def signatures = inputs.signatures

		assert signatures, "This method requires a map of signatures"

		log.info "Signing the PDF for document ${document}"

		PdfReader pdfReader = new PdfReader(data.data)

		ByteArrayOutputStream output = new ByteArrayOutputStream()
		PdfStamper pdfStamper = new PdfStamper(pdfReader, output)

		(1..data.pages).each { i ->
			byte[] imageData = signatures[i.toString()]
			if (!imageData) {
				return
			}

			PdfContentByte content = pdfStamper.getOverContent(i)
			Rectangle psize = pdfReader.getPageSize(i);

			Image image = Image.getInstance(imageData)
			image.scaleAbsolute psize.getWidth(), psize.getHeight()
			image.setAbsolutePosition(0f, 0f)

			content.addImage(image)

			handlerChain.sign(document: document, documentData: document.previewImage(i).data,
					signature: imageData)
		}

		pdfStamper.close()

		DocumentData newPdf = new DocumentData(mimeType: data.mimeType, pages: data.pages)
		newPdf.data = output.toByteArray()
		document.addToFiles(newPdf)
	}
}
