package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

@MultiTenant
class Highlight {
	Date accepted
	int height
	int _left
	int pageNumber
	boolean required = false
	int width
	int top

	static belongsTo = [party:Party]

	static constraints = {
		accepted nullable:true
		height min:0
		_left min:0
		pageNumber min:1, max:10000
		width min:0
		top min:0
	}

	Map toMap() {
		[height:height, left:_left, width:width, top:top]
	}
}
