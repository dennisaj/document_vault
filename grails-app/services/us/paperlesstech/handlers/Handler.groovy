package us.paperlesstech.handlers

import us.paperlesstech.DocumentData
import us.paperlesstech.Document

class Handler {
	void importFile(Map inputs) {
		throw new UnsupportedOperationException("importFile has no handler for ${data.mimeType}")
	}

	void generatePreview(Map inputs) {
		throw new UnsupportedOperationException("generatePreview has no handler for ${data.mimeType}")
	}

	void print(Map inputs) {
		throw new UnsupportedOperationException("print has no handler for ${data.mimeType}")
	}

	void sign(Map inputs) {
		throw new UnsupportedOperationException("sign has no handler for ${data.mimeType}")
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
