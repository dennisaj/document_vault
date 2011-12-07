package us.paperlesstech

import grails.plugin.spock.UnitSpec

import javax.servlet.http.HttpServletRequest

import org.codehaus.groovy.grails.web.servlet.HttpHeaders

class RequestServiceSpec extends UnitSpec {
	RequestService service = new RequestService()
	HttpServletRequest request = Mock()

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

	def "should be able to get the remote addr"() {
		given: "a test request object"
		service.testRequest = request

		when: "Get the remote request"
		def t1 = service.remoteAddr

		then: "The address is returned"
		1 * request.getRemoteAddr() >> addr
		t1 == addr

		where:
		addr = "addr"
	}

	def "should be able to retrieve values from the header"() {
		given: "a test request object"
		service.testRequest = request

		when: "get something from the header"
		def t1 = service.getHeader(field)

		then: "The address is returned"
		1 * request.getHeader(field) >> browser
		t1 == browser

		where:
		field = "User-Agent"
		browser = "FF"
	}

	def "getRemoteAddr should return X-Forwarded-For if it is set"() {
		given: "a test request object"
		service.testRequest = request

		when: "Get the remote request"
		def t1 = service.remoteAddr

		then: "The address is returned"
		1 * request.getHeader(HttpHeaders.X_FORWARDED_FOR) >> addr
		0 * request.getRemoteAddr()
		t1 == addr

		where:
		addr = "127.0.0.1"
	}

	def "getRemoteAddr should return remoteAddr if X-Forwarded-For is not set"() {
		given: "a test request object"
		service.testRequest = request

		when: "Get the remote request"
		def t1 = service.remoteAddr

		then: "The address is returned"
		1 * request.getHeader(HttpHeaders.X_FORWARDED_FOR) >> ""
		1 * request.getRemoteAddr() >> addr
		t1 == addr

		where:
		addr = "127.0.0.1"
	}

	def 'getBaseAddr should parse the host from the Host header'() {
		service.testRequest = request

		when:
		def result = service.baseAddr

		then:
		result == output
		request.getHeader(HttpHeaders.HOST) >> host

		where:
		host             | output
		'localhost'      | 'http://localhost'
		'localhost:443'  | 'https://localhost'
		'localhost:8080' | 'http://localhost:8080'
	}
}
