package us.paperlesstech

import org.codehaus.groovy.grails.web.json.JSONObject

import spock.lang.Specification
import grails.test.mixin.TestFor

@TestFor(HighlightService)
class HighlightServiceSpec extends Specification {
	HighlightService service
	AuthService authService = Mock()

	def setup() {
		service = new HighlightService()
		service.authService = authService
	}

	def "test fromJsonList"() {
		given:
			def party = new Party(id:1)
			def l = [JSONObject.NULL, [[left:10, top:20, width:30, height:40], JSONObject.NULL, null], JSONObject.NULL, [JSONObject.NULL, [left:50, top:60, width:70, height:80], null]]
		when:
			def out = service.fromJsonList(party, l)
		then:
			out.size() == 2

			out[0].pageNumber == 1
			out[0].party == party
			out[0].left == l[1][0].left
			out[0].top == l[1][0].top
			out[0].width == l[1][0].width
			out[0].height == l[1][0].height

			out[1].pageNumber == 3
			out[1].party == party
			out[1].left == l[3][1].left
			out[1].top == l[3][1].top
			out[1].width == l[3][1].width
			out[1].height == l[3][1].height
	}

	def "test fromMap"() {
		given:
			def m = [left:1, top:2, width:3, height:4]
		when:
			def h = service.fromMap(m)
		then:
			h.left == m.left
			h.top == m.top
			h.width == m.width
			h.height == m.height
	}
}
