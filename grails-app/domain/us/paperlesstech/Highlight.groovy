package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

import org.codehaus.groovy.grails.web.json.JSONObject

@MultiTenant
class Highlight {

	int lowerRightX
	int lowerRightY
	int pageNumber
	boolean required = true
	int upperLeftX
	int upperLeftY

	static belongsTo = [party:Party]

	static constraints = {
		lowerRightX min:0
		lowerRightY min:0
		pageNumber min:1, max:10000
		upperLeftX min:0
		upperLeftY min:0
	}

	Map toMap() {
		[lowerRightCorner:[x:lowerRightX, y:lowerRightY], upperLeftCorner:[x:upperLeftX, y:upperLeftY]]
	}

	static List fromJsonList(Party party, List jsonHighlights) {
		def highlights = []
		jsonHighlights?.eachWithIndex {page, pageNumber->
			if (page && page != JSONObject.NULL) {
				page.each {highlight->
					if (highlight && highlight != JSONObject.NULL) {
						def h = fromMap(highlight)
						h.pageNumber = pageNumber
						h.party = party
						highlights.add(h)
					}
				}
			}
		}

		highlights
	}

	static Highlight fromMap(Map corners) {
		new Highlight(
			lowerRightX:corners.b.x,
			lowerRightY:corners.b.y,
			upperLeftX:corners.a.x,
			upperLeftY:corners.a.y)
	}
}
