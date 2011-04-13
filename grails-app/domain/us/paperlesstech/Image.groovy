package us.paperlesstech

class Image {
	static belongsTo = [document:Document]
	byte[] data
	int pageNumber
	int sourceHeight
	int sourceWidth

    static constraints = {
		pageNumber(unique:"document")
		data(maxSize:20*1024*1024)
    }
}