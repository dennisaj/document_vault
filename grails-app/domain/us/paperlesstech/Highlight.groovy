package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

@MultiTenant
class Highlight {
	Date accepted
	int lowerRightX
	int lowerRightY
	int pageNumber
	boolean required = false
	int upperLeftX
	int upperLeftY

	static belongsTo = [party:Party]

	static constraints = {
		accepted nullable:true
		lowerRightX min:0
		lowerRightY min:0
		pageNumber min:1, max:10000
		upperLeftX min:0
		upperLeftY min:0
	}

	Map toMap() {
		[lowerRightCorner:[x:lowerRightX, y:lowerRightY], upperLeftCorner:[x:upperLeftX, y:upperLeftY]]
	}
}
