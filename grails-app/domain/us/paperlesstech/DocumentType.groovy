package us.paperlesstech

/**
 * Defines the different document types for an organization
 */
class DocumentType {
	String name

    static constraints = {
    }
	
	String toString() {
		"DocumentType(${id}) - ${name}"
	}
}