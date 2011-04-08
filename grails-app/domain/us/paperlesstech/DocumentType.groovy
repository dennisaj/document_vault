package us.paperlesstech

/**
 * Defines the different document types for an organization
 */
class DocumentType {
	static hasMany = [searchOptions:String]
	String name
	List searchOptions

    static constraints = {
    }
	
	String toString() {
		"DocumentType(${id}) - ${name}"
	}
}