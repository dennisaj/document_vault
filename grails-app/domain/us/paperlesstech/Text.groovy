package us.paperlesstech

class Text {
	static belongsTo = [document: Document]
	static searchable = true
	
	String raw
	Map<String, String> parsedFields

	static constraints = {
		raw(nullable:true, blank:true, maxSize:20*1024*1024)
	}
}