package us.paperlesstech

import org.codehaus.groovy.grails.web.servlet.HttpHeaders
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class RequestService {
	static transactional = false
	def testRequest

	String getBaseAddr() {
		def host = getHeader(HttpHeaders.HOST)
		assert host

		def parts = host.split(':')
		assert parts.size() == 2

		if (parts[1] == '443') {
			return "https://${parts[0]}"
		} else {
			return "http://${parts[0]}"
		}
	}

	def getRealRequest() {
		def request = testRequest ?: ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		assert request, "A request is required"
		request
	}

	Object get(String entry) {
		getRealRequest().getAttribute(entry)
	}

	String getHeader(String field) {
		getRealRequest().getHeader(field)
	}

	String getRemoteAddr() {
		getRealRequest().getHeader(HttpHeaders.X_FORWARDED_FOR) ?: getRealRequest().getRemoteAddr()
	}

	void set(String entry, Object value) {
		getRealRequest().setAttribute(entry, value)
	}

	String getRequestURI() {
		getRealRequest().requestURI
	}

	String getRequestURL() {
		getRealRequest().requestURL
	}
}