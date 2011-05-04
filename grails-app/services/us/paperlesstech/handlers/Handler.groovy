package us.paperlesstech.handlers

import us.paperlesstech.DocumentData
import us.paperlesstech.Document

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
