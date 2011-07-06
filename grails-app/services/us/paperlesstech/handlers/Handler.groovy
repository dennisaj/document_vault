package us.paperlesstech.handlers

import us.paperlesstech.Document
import us.paperlesstech.DocumentData

class Handler {
	def authServiceProxy
	def fileService

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

		def filename = "${d.toString()} - page($page)${d.previewImage(page).data.mimeType.getDownloadExtension()}"

		[filename, fileService.getInputStream(previewImage.data), previewImage.data.mimeType.downloadContentType,
				previewImage.data.fileSize]
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
