package us.paperlesstech

import grails.plugin.spock.*

import org.codehaus.groovy.grails.web.json.JSONObject

import spock.lang.*

class HighlightSpec extends UnitSpec {
	def "test toMap"() {
		given:
			def h = new Highlight(height: 10, _left:20, width:30, top:40)
		when:
			def m = h.toMap()
		then:
			m.height == h.height
			m.left == h._left
			m.width == h.width
			m.top == h.top
	}
}
