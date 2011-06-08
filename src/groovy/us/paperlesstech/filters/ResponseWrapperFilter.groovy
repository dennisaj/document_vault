package us.paperlesstech.filters

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletResponse

class ResponseWrapperFilter implements Filter {
	void destroy() {}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
		response = response instanceof ResponseWrapper ?: new ResponseWrapper((HttpServletResponse) response)

		chain.doFilter(request, response);
	}

	void init(FilterConfig filterConfig) {}

	static ResponseWrapper getResponseWrapper(ServletResponse response) {
		def foundResponse
		20.times {
			if (!response || response instanceof ResponseWrapper) {
				foundResponse = response
				return
			}

			response = response.response
		}

		foundResponse
	}
}
