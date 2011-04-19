package us.paperlesstech

import javax.servlet.http.HttpServletRequest

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class RequestService {
	static transactional = false
	def testRequest

	def getRealRequest() {
		return testRequest ?: ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
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
}