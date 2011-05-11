package us.paperlesstech.handlers

import us.paperlesstech.Document
import us.paperlesstech.DocumentData
import us.paperlesstech.PreviewImage

class Handler {
	void importFile(Map input) {
		throw new UnsupportedOperationException("importFile has no handler for ${input.documentData.mimeType}")
	}

	void generatePreview(Map input) {
		throw new UnsupportedOperationException("generatePreview has no handler for ${input.documentData.mimeType}")
	}

	void print(Map input) {
		throw new UnsupportedOperationException("print has no handler for ${input.documentData.mimeType}")
	}

	void sign(Map input) {
		throw new UnsupportedOperationException("sign has no handler for ${input.documentData.mimeType}")
	}

	def retrievePreview(Map input) {
		def d = getDocument(input)

		def page = input.page
		assert page, "This method requires a page number"

		def previewImage = d.previewImage(page)
		assert previewImage, "No preview image exists for page '$page'"

		def filename = "${d.toString()} - page($page)${d.previewImage(page).data.mimeType.getDownloadExtension()}"

		[filename, previewImage.data.data, previewImage.data.mimeType.downloadContentType]
	}

	def download(Map input) {
		def d = getDocument(input)
		def data = getDocumentData(input)

		def filename = d.toString() +data.mimeType.getDownloadExtension()

		[filename, data.data, data.mimeType.downloadContentType]
	}

	static Document getDocument(Map map) {
		def document = map?.document
		assert document instanceof Document, "Must pass Document with name document"
		document
	}

	static DocumentData getDocumentData(Map map) {
		def data = map?.documentData
		assert data instanceof DocumentData, "Must pass DocumentData with name documentData"
		data
	}
}
