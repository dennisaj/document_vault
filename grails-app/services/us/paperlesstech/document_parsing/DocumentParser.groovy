package us.paperlesstech.document_parsing

import us.paperlesstech.Document
import us.paperlesstech.DocumentType

/**
 * Base class for document parsing classes
 */
abstract class DocumentParser {
	/**
	 * Parses the given document and returns the Document Type
	 * 
	 * @param d the document to parse
	 * 
	 * @return the Document Type of the given document
	 */
	abstract DocumentType getDocumentType(Document d)

	/**
	 * Parses the given document and returns a map of the data in the document
	 * 
	 * @param d the document to parse
	 * 
	 * @return a map of the data in the document
	 */
	abstract Map parseDocument(Document d)
}