package us.paperlesstech

import grails.plugin.spock.*

import org.codehaus.groovy.grails.web.json.JSONObject

import spock.lang.*

class HighlightSpec extends UnitSpec {
	def "test toMap"() {
		given:
			def h = new Highlight(lowerRightX: 10, lowerRightY:20, upperLeftX:30, upperLeftY:40)
		when:
			def m = h.toMap()
		then:
			m.lowerRightCorner.x == h.lowerRightX
			m.lowerRightCorner.y == h.lowerRightY 
			m.upperLeftCorner.x == h.upperLeftX
			m.upperLeftCorner.y == h.upperLeftY
	}
}
