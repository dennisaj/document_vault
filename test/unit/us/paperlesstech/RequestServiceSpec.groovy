package us.paperlesstech

import javax.servlet.http.HttpServletRequest;

import spock.lang.*
import grails.plugin.spock.*

class RequestServiceSpec extends UnitSpec {
	RequestService service = new RequestService()
	HttpServletRequest request = Mock()

	def "should throw an exception when not inside a request"() {
		when: "Called outside a request"
		service.realRequest

		then: "An IllegalStateException should be thrown"
		thrown(IllegalStateException)
	}

	def "should allow getting like a map"() {
		given: "a test request object"
		service.testRequest = request

		when: "Get with dot notation"
		def t1 = service.field1
		and: "Get with hash notation"
		def t2 = service["field2"]
		def t3 = service.unknown

		then: "request.getAttribute() was called twice"
		3 * request.getAttribute(_) >>> [value1, value2, null]
		and: "The expected values were returned from the test"
		t1 == value1
		t2 == value2
		t3 == null

		where:
		value1 = "value1"
		value2 = "value2"
	}

	def "should allow setting like a map"() {
		given: "a test request object"
		service.testRequest = request

		when: "Set with dot notation"
		service.field1 = "value1"
		and: "Set with hash notation"
		service["field2"] = "value2"

		then: "request.setAttribute() was called twice"
		2 * request.setAttribute({ it.startsWith("field") }, { it.startsWith("value") })
	}
}
