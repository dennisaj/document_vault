package us.paperlesstech

import spock.lang.Specification

class HighlightSpec extends Specification {
	def "test toMap"() {
		given:
			def h = new Highlight(party: new Party(id: 1), height: 10, left:20, width:30, top:40)
		when:
			def m = h.asMap()
		then:
			m.partyId == h.party.id
			m.height == h.height
			m.left == h.left
			m.width == h.width
			m.top == h.top
	}
}
