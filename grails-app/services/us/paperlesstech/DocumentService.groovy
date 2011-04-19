package us.paperlesstech

import java.awt.Graphics2D
import java.awt.RenderingHints

import javax.imageio.ImageIO

import us.paperlesstech.module.document_parsing.ferman.FermanDocumentType

import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper


class DocumentService {
	static transactional = true
	static imageDataPrefix = "data:image/png;base64,"

	def activityLogService

	/**
	 * Removes the extension from the file name.
	 * 
	 * <pre>
	 * assert chopExtension("/tmp/test.pdf", ".pdf") == "/tmp/test"
	 * </pre>
	 * 
	 * @param fileName the string name of the file
	 * @param extension the extension including the period
	 * 
	 * @return The filename without the extension
	 */
	def chopExtension(String fileName, String extension) {
		int chopLength = (extension.size() + 1) * -1
		return fileName[0 .. chopLength]
	}

	def createImagesFromPcl(Document d) {
		assert d != null
		assert d.pcl != null
		assert d.pcl.data != null

		List filesToDelete = []

		File pclFile = File.createTempFile("vault", ".pcl")
		filesToDelete += pclFile

		String baseName = chopExtension(pclFile.getAbsolutePath(), ".pcl")

		try {
			pclFile.setBytes(d.pcl.data)

			// -r150 creates a file with 150dpi, we find this readable even while zoomed on iPhone but it can be changed
			def cmd = """/usr/local/bin/pcl6 -dNOPAUSE -dBATCH -dFirstPage=3 -sDEVICE=pngmono -r150 -sOutputFile=${baseName}-%d.png ${pclFile.getAbsolutePath()}"""
			log.debug "Image create - ${cmd}"
			def proc = cmd.execute()
			proc.waitFor()
			if(proc.exitValue()) {
				throw new RuntimeException("Unable to process file for document ${d.id} - PCL to PNG conversion failed")
			}

			assert d.pdf != null
			assert d.pdf.data != null
			PdfReader pdfReader = new PdfReader(d.pdf.data)
			int pageCount = pdfReader.getNumberOfPages()

			for(int page = 1; page <= pageCount; page++) {
				File f = new File("${baseName}-${page}.png")
				if(!f.exists() || !f.canRead()) {
					break;
				}
				filesToDelete += f

				Rectangle psize = pdfReader.getPageSize(page);

				Image i = new Image(pageNumber:page, sourceWidth:psize.getWidth(), sourceHeight:psize.getHeight())
				i.data = f.getBytes()
				d.addToImages(i)
			}

			pdfReader.close();

			assert d.images.size() > 0
		} finally {
			filesToDelete.each { it.delete() }
		}

		log.info "Images created for document ${d}"
		d.save()
	}

	def createPdfFromPcl(Document d) {
		assert d != null
		assert d.pcl != null
		assert d.pcl.data != null

		File pclFile = File.createTempFile("vault", ".pcl")
		String baseName = chopExtension(pclFile.getAbsolutePath(), ".pcl")
		File pdfFile = new File(baseName + ".pdf")

		try {
			pclFile.setBytes(d.pcl.data)

			// TODO FirstPage is only 3 when the file has the logos appended to the head of the pcl
			def cmd = """/usr/local/bin/pcl6 -dNOPAUSE -dBATCH -dFirstPage=3 -sDEVICE=pdfwrite -sOutputFile=${baseName}.pdf ${pclFile.getAbsolutePath()}"""
			log.debug "PDF create - ${cmd}"
			def proc = cmd.execute()
			proc.waitFor()
			if(proc.exitValue()) {
				throw new RuntimeException("Unable to process file for document ${d.id} - PCL to PDF conversion failed")
			}

			Pdf pdf = new Pdf()
			pdf.data = pdfFile.getBytes()
			d.pdf = pdf
		} finally {
			if(pdfFile) {
				pdfFile.delete()
			}

			if(pclFile) {
				pclFile.delete()
			}
		}

		log.info "Pdf created for document ${d}"
		d.save()
	}

	def createTextFromPcl(Document d) {
		FermanDocumentType fdt = new FermanDocumentType()
		DocumentType type = fdt.getDocumentType(d)
		d.type = type

		def text = fdt.parseDocument(d)
		d.text = text
		if(!d.text.parsedFields) d.text.parsedFields = [:]
		d.text.parsedFields.DocumentType = d.type.name

		log.info "Text saved for document ${d} - identified as ${d.type}"
		d.save()
	}

	/**
	 * Loads the specified document and returns the image data for the given page.
	 * The actual image will be base64 encoded.
	 * 
	 * @param documentId The id of the document to load
	 * @param pageNumber Retrieve the data for this page
	 * 
	 * @return A map of the data contained on the image for the given page.
	 */
	def getImageDataAsMap(documentId, pageNumber) {
		def document = Document.get(documentId)
		assert document, "Cannot load document - $documentId"

		def images = document.getSortedImages()
		assert images, "${document} has no images."

		// Make sure that the page number falls within the list of pages for this document
		pageNumber = (0 ..< images.size).contains(pageNumber) ? pageNumber : 0
		def image = images[pageNumber]

		[imageData: imageDataPrefix + image.data.encodeBase64().toString(),
					pageNumber: pageNumber,
					sourceHeight: image.sourceHeight,
					sourceWidth: image.sourceWidth]
	}

	def signDocument(Document d, Map signatures) {
		log.info "Signing the PDF for document ${d}"

		PdfReader pdfReader = new PdfReader(d.pdf.data)

		ByteArrayOutputStream output = new ByteArrayOutputStream()
		PdfStamper pdfStamper = new PdfStamper(pdfReader, output)

		int pageCount = d.getSortedImages().size()
		for(int i = 0; i < pageCount; i++) {
			byte[] imageData = signatures[i.toString()]
			if(!imageData) {
				continue
			}

			// iText is 1 based where as we are 0 based
			PdfContentByte content = pdfStamper.getOverContent(i + 1)
			Rectangle psize = pdfReader.getPageSize(i + 1);

			com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(imageData)
			image.scaleAbsolute psize.getWidth(), psize.getHeight()
			image.setAbsolutePosition(0f, 0f)

			content.addImage(image)
		}

		pdfStamper.close()

		// Replace the existing pdf
		d.pdf.data = output.toByteArray()
		d.save()

		// Recreate the images
		signImages d, signatures
	}

	def signImages(Document d, Map signatures) {
		log.info "Updating the images for document ${d}"

		PdfReader pdfReader = new PdfReader(d.pdf.data)

		int pageCount = d.getSortedImages().size()
		for(int i = 0; i < pageCount; i++) {
			byte[] imageData = signatures[i.toString()]
			if(!imageData) {
				continue
			}

			long start = System.currentTimeMillis()
			Image image = d.getSortedImages()[i]
			java.awt.Image original = ImageIO.read(new ByteArrayInputStream(image.data))
			java.awt.Image signature = ImageIO.read(new ByteArrayInputStream(imageData))
			signature = signature.getScaledInstance(original.width, original.height, java.awt.Image.SCALE_SMOOTH)

			Graphics2D buffer = original.createGraphics()
			buffer.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
			buffer.drawImage(original, null, null)
			buffer.drawImage(signature, null, null)
			buffer.dispose()

			ByteArrayOutputStream output = new ByteArrayOutputStream()
			ImageIO.write(original, "png", output)
			image.data = output.toByteArray()
			long end = System.currentTimeMillis()
			System.err.println("It took ${end - start}ms")
		}

		d.save()
	}

	/**
	 * Loads the specified document and adds the signatures to it
	 * 
	 * @param signatures A map of the raw signature image data for each page of the document
	 * @param pageNumber The page number the signature goes on
	 * @param imageData Base64 encoded image of the signature
	 * 
	 * @return the map with the decoded images
	 */
	def saveSignatureToMap(Map<Integer, Byte[]> signatures, pageNumber, imageData) {
		assert imageData

		def decodedData = imageData[imageDataPrefix.size() .. -1].decodeBase64()

		signatures[pageNumber] = decodedData
	}
}