package us.paperlesstech

import javax.servlet.http.HttpServletRequest

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class RequestService {
	static transactional = false
	def testRequest

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
		getRealRequest().getRemoteAddr()
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