package us.paperlesstech

class Image {
	static belongsTo = [document:Document]
	byte[] data
	int pageNumber

    static constraints = {
		pageNumber(unique:"document")
    }
	
	static mapping = {
	}
}