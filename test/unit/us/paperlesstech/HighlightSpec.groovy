package us.paperlesstech

import spock.lang.Specification

class HighlightSpec extends Specification {
	def "test toMap"() {
		given:
			def h = new Highlight(height: 10, left:20, width:30, top:40)
		when:
			def m = h.toMap()
		then:
			m.height == h.height
			m.left == h.left
			m.width == h.width
			m.top == h.top
	}
}
