package us.paperlesstech

class Image {
	static belongsTo = [document:Document]
	byte[] data
	int pageNumber
	int sourceHeight
	int sourceWidth

    static constraints = {
		pageNumber(unique:"document")
    }
	
	static mapping = {
	}
}