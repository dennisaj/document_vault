package us.paperlesstech

class Document {
	DocumentType type
	boolean viewed = false
	boolean signed = false
	Date dateCreated

	// Ideally I would map these as static hasOne = which would put the FK in these respective
	// tables instead of the Document table.  However currently that mapping forces grails to
	// eagerly fetch the data and we don't want to eagerly fetch a huge PDF.

	Pcl pcl
	Pdf pdf
	
	static hasMany = [images: Image]
	
	def getSortedImages = {
		return images.sort { it.pageNumber } as List
	}

	static mapping = {
	}

	static constraints = {
		type(nullable: true)
		pcl(unique: true, nullable: true)
		pdf(unique: true, nullable: true)
	}

	String toString() {
		"Document(${id})"
	}
}
