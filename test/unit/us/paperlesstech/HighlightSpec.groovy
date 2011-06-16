package us.paperlesstech

import grails.plugin.spock.*

import org.codehaus.groovy.grails.web.json.JSONObject

import spock.lang.*

class HighlightSpec extends UnitSpec {

	def "test fromJsonList"() {
		given:
			def party = new Party(id:1)
			def l = [JSONObject.NULL, [[a:[x:10, y:20], b:[x:30, y:40]], JSONObject.NULL, null], JSONObject.NULL, [JSONObject.NULL, [a:[x:50, y:60], b:[x:70, y:80]], null]]
		when:
			def out = Highlight.fromJsonList(party, l)
		then:
			out.size() == 2

			out[0].pageNumber == 1
			out[0].party == party
			out[0].lowerRightX == l[1][0].b.x
			out[0].lowerRightY == l[1][0].b.y
			out[0].upperLeftX == l[1][0].a.x
			out[0].upperLeftY == l[1][0].a.y

			out[1].pageNumber == 3
			out[1].party == party
			out[1].lowerRightX == l[3][1].b.x
			out[1].lowerRightY == l[3][1].b.y
			out[1].upperLeftX == l[3][1].a.x
			out[1].upperLeftY == l[3][1].a.y
	}

	def "test fromMap"() {
		given:
			def m = [a:[x:10, y:20], b:[x:30, y:40]]
		when:
			def h = Highlight.fromMap(m)
		then:
			h.lowerRightX == m.b.x
			h.lowerRightY == m.b.y
			h.upperLeftX == m.a.x
			h.upperLeftY == m.a.y
	}

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
