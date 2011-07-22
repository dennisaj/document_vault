package us.paperlesstech

import org.codehaus.groovy.grails.web.json.JSONObject

class HighlightService {
	static transactional = true

	def authServiceProxy

	/**
	* Sets {@link Highlight#accepted} dates to now.
	*
	* @return The updated Highlight.
	*
	* @throws RuntimeException If there is a error saving the Highlight.
	*/
	Highlight markAccepted(Highlight highlight) {
		assert highlight
		assert authServiceProxy.canSign(highlight.party.document) || authServiceProxy.canGetSigned(highlight.party.document)

		if (highlight.party.rejected || highlight.accepted) {
			return highlight
		}

		highlight.accepted = new Date()

		def savedHighlight = highlight.save()
		if (savedHighlight) {
			return savedHighlight
		}

		throw new RuntimeException("Unable to mark Highlight(${highlight.id}) accepted.")
	}

	List fromJsonList(Party party, List jsonHighlights) {
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

	Highlight fromMap(Map corners) {
		new Highlight(
			_left:corners.left,
			top:corners.top,
			width:corners.width,
			height:corners.height)
	}
}
