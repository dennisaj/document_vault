package us.paperlesstech

/**
* Stores the PDF of this document
*/
class Pdf {
	static belongsTo = [document: Document]
	byte[] data

	static constraints = {
	}
}