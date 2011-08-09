package us.paperlesstech.handlers

import java.awt.image.BufferedImage

import javax.imageio.ImageIO

import us.paperlesstech.Document
import us.paperlesstech.DocumentData
import us.paperlesstech.helpers.ImageHelpers


class Handler {
	def authServiceProxy
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
	 * Converts the document into a printable format and inserts it into the PrintQueue.  Expects an element with key document
	 *
	 * @param input the map to retrieve document and documentData from
	 */
	void print(Map input) {
		throw new UnsupportedOperationException("print has no handler for ${input?.documentData?.mimeType}")
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
		assert authServiceProxy.canTag(d) || authServiceProxy.canView(d) || authServiceProxy.canSign(d)

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
		assert authServiceProxy.canTag(d) || authServiceProxy.canView(d) || authServiceProxy.canSign(d)

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
		assert authServiceProxy.canView(d) || authServiceProxy.canSign(d)
		def data = getDocumentData(input)

		def filename = d.toString() + data.mimeType.getDownloadExtension()

		[filename, fileService.getInputStream(data), data.mimeType.downloadContentType, data.fileSize]
	}

	/**
	 * Applies the given lines (an object called "notes" that maps from DocumentData.id to a list of lines) 
	 * to the existing notes or creates new notes if the id is not a valid DocumentData
	 *
	 */
	def saveNotes(Map input) {
		def d = getDocument(input)
		assert authServiceProxy.canNotes(d)
		def notes = input.notes
		assert notes, "This method requires notes"

		def width = grailsApplication.config.document_vault.document.note.defaultWidth
		def height = grailsApplication.config.document_vault.document.note.defaultHeight
		def mimeType = grailsApplication.config.document_vault.document.note.defaultMimeType

		notes.each {key, lines ->
			def note = DocumentData.get(key)
			BufferedImage original

			if (note) {
				assert note in d.notes
				mimeType = note.mimeType
				fileService.withInputStream(note) {is->
					original = ImageIO.read(is)
				}

				d.removeFromNotes(note)
			} else {
				original = ImageHelpers.createBlankImage(width, height)
			}

			ImageHelpers.drawLines(original, lines)

			ByteArrayOutputStream output = new ByteArrayOutputStream()
			ImageIO.write(original, mimeType.toString().toLowerCase(), output)

			DocumentData newImage = fileService.createDocumentData(bytes:output.toByteArray(), mimeType:mimeType, pages:1)
			d.addToNotes(newImage)
		}
	}

	/**
	 * Returns a quad of the documentNote image to be downloaded to the client. NOTE: The returned InputStream must be
	 * closed by the caller.
	 *
	 * @param input a map that must contain document and documentNote
	 * @return a quad of (documentFilename, document data as an Input Stream, document mimetype, and content length
	 */
	def downloadNote(Map input) {
		def d = getDocument(input)
		assert authServiceProxy.canNotes(d)

		def documentNote = input.documentNote
		assert documentNote, "This method requires a document note"

		def filename = "${d.toString()}-documentNote($documentNote.id).$documentNote.mimeType.downloadExtension"

		[filename, fileService.getInputStream(documentNote), documentNote.mimeType.downloadContentType, documentNote.fileSize]
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
