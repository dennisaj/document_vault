package us.paperlesstech

class Text {
	static belongsTo = [document: Document]
	static searchable = true
	
	String raw
	Map<String, String> parsedFields

	static constraints = {
		raw(nullable:true, blank:true)
	}
	
	static mapping = {
		raw(type:"text")
	}
}