package us.paperlesstech.handlers

import java.awt.image.BufferedImage

import javax.imageio.ImageIO

import us.paperlesstech.Document
import us.paperlesstech.DocumentData
import us.paperlesstech.Note
import us.paperlesstech.helpers.ImageHelpers


class Handler {
	def authService
	def fileService
	def grailsApplication

	/**
	 * Imports the document in the map.  This includes generating previews.  documentData and the perviews will be
	 * added to the document. Expects elements with keys document and documentData
	 *
	 * @param input the map to retrieve document and documentData from
	 */
	void importFile(Map input) {
		throw new UnsupportedOperationException("importFile has no handler for ${input?.documentData?.mimeType}")
	}

	/**
	 * Generates a preview for the passed documentData and inserts it into document.  Expects elements with keys document and
	 * documentData
	 *
	 * @param input the map to retrieve document and documentData from
	 */
	void generatePreview(Map input) {
		throw new UnsupportedOperationException("generatePreview has no handler for ${input?.documentData?.mimeType}")
	}

	/**
	 * Converts the document into a printable format and prints it to the passed printer.  Expect elements with the
	 * key document and one with the key printer
	 *
	 * @param input the map to retrieve document and printer from
	 *
	 * @return true if the document is successfully printed
	 */
	boolean print(Map input) {
		throw new UnsupportedOperationException("print has no handler for ${input?.document?.files?.first?.mimeType}")
	}

	/**
	 * Applies the passed signatures to the document.  This will not alter the original document but insert a copy
	 * into the files array.  It may also call generatePreview to create new preview images.
	 * Expects elements with keys document and documentData and signatures
	 *
	 * @param input the map to retrieve document and documentData and signatures from
	 */
	void cursiveSign(Map input) {
		throw new UnsupportedOperationException("cursiveSign has no handler for ${input?.documentData?.mimeType}")
	}

	/**
	 * Returns a quad of the preview image to be downloaded to the client. NOTE: The returned InputStream must be
	 * closed by the caller.
	 *
	 * @param input a map that must contain document and page
	 * @return a quad of (documentFilename, document data as an Input Stream, document mimetype, and content length
	 */
	def downloadPreview(Map input) {
		def d = getDocument(input)
		assert authService.canView(d) || authService.canSign(d)

		def page = input.page
		assert page, "This method requires a page number"

		def previewImage = d.previewImage(page)
		assert previewImage, "No preview image exists for page '$page'"

		def filename = "${d.toString()}-page($page)${d.previewImage(page).data.mimeType.getDownloadExtension()}"

		[filename, fileService.getInputStream(previewImage.data), previewImage.data.mimeType.downloadContentType,
				previewImage.data.fileSize]
	}

	/**
	 * Returns a quad of the thumbnail image to be downloaded to the client. NOTE: The returned InputStream must be
	 * closed by the caller.
	 *
	 * @param input a map that must contain document and page
	 * @return a quad of (documentFilename, document data as an Input Stream, document mimetype, and content length
	 */
	def downloadThumbnail(Map input) {
		def d = getDocument(input)
		assert authService.canView(d) || authService.canSign(d)

		def page = input.page
		assert page, "This method requires a page number"

		def previewImage = d.previewImage(page)
		assert previewImage, "No preview image exists for page '$page'"

		def filename = "${d.toString()}-page($page)-thumbnail-${d.previewImage(page).thumbnail.mimeType.getDownloadExtension()}"

		[filename, fileService.getInputStream(previewImage.thumbnail), previewImage.thumbnail.mimeType.downloadContentType, previewImage.thumbnail.fileSize]
	}

	/**
	 * Returns a quad of the document to be downloaded to the client. NOTE: The returned InputStream must be closed
	 * by the caller.
	 *
	 * @param input a map that must contain document and documentData to be downloaded
	 * @return a quad of (documentFilename, document data as an Input Stream, document mimetype, and content length
	 */
	def download(Map input) {
		def d = getDocument(input)
		assert authService.canView(d) || authService.canSign(d)
		def data = getDocumentData(input)

		def filename = d.toString() + data.mimeType.getDownloadExtension()

		[filename, fileService.getInputStream(data), data.mimeType.downloadContentType, data.fileSize]
	}

	/**
	 * Takes a list of Maps in a variable called notes that contain two entries: 'lines' and 'text'.
	 * The map may also optionally include 'top', 'left' and 'pageNumber.' However, if pageNumber is not set, top and left will be ignored.
	 * <br><br>
	 * e.g.: input.notes = [[text:"this is some text", lines:[/ * Line Data Goes here * /]]
	 */
	def saveNotes(Map input) {
		def d = getDocument(input)
		assert authService.canNotes(d)
		def notes = input.notes
		assert notes, "This method requires notes"

		def width = grailsApplication.config.document_vault.document.note.defaultWidth
		def height = grailsApplication.config.document_vault.document.note.defaultHeight
		def mimeType = grailsApplication.config.document_vault.document.note.defaultMimeType

		notes.each { entry->
			assert entry.text || entry.lines, "A note must contain either text or lines"

			def note = new Note(user:authService.authenticatedUser, note:entry.text)

			if (entry.pageNumber) {
				note.pageNumber = entry.pageNumber
				note.left = entry.left
				note.top = entry.top
			}

			if (entry.lines) {
				BufferedImage original = ImageHelpers.createBlankImage(width, height)
				ImageHelpers.drawLines(original, entry.lines)
				ByteArrayOutputStream output = new ByteArrayOutputStream()
				ImageIO.write(original, mimeType.toString().toLowerCase(), output)

				DocumentData newImage = fileService.createDocumentData(bytes:output.toByteArray(), mimeType:mimeType, pages:1)
				note.data = newImage
			}

			d.addToNotes(note)
		}
	}

	/**
	 * Returns a quad of the documentNote image to be downloaded to the client. NOTE: The returned InputStream must be
	 * closed by the caller.
	 *
	 * @param input a map that must contain document and note
	 * @return a quad of (documentFilename, document data as an Input Stream, document mimetype, and content length
	 */
	def downloadNote(Map input) {
		def d = getDocument(input)
		assert authService.canNotes(d)

		def note = input.note
		assert note?.data, "This method requires a note with DocumentData"

		def filename = "${d.toString()}-note($note.id)-${note.data.id}.$note.data.mimeType.downloadExtension"

		[filename, fileService.getInputStream(note.data), note.data.mimeType.downloadContentType, note.data.fileSize]
	}

	/**
	 * Returns the document from the passed map
	 *
	 * @param map the map to search for the document key
	 * @return Always returns the document
	 * @throws AssertionError if document isn't in the map or isn't of type Document
	 */
	static Document getDocument(Map map) {
		def document = map?.document
		assert document instanceof Document, "Must pass Document with name document"
		document
	}

	/**
	 * Returns the documentData from the passed map
	 *
	 * @param map the map to search for the documentData key
	 * @return Always returns the documentData
	 * @throws AssertionError if documentData isn't in the map or isn't of type DocumentData
	 */
	static DocumentData getDocumentData(Map map) {
		def data = map?.documentData
		assert data instanceof DocumentData, "Must pass DocumentData with name documentData"
		data
	}


	/**
	 * Sets document in the passed map
	 *
	 * @param map The map to be altered
	 * @param document The value to insert into the map
	 */
	static void setDocument(Map map, Document document) {
		assert map instanceof Map && document
		map.document = document
	}

	/**
	 * Sets documentData in the passed map
	 *
	 * @param map The map to be altered
	 * @param documentData The value to insert into the map
	 */
	static void setDocumentData(Map map, DocumentData documentData) {
		assert map instanceof Map && documentData
		map.documentData = documentData
	}
}
