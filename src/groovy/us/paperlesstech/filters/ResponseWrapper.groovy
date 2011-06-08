package us.paperlesstech.filters

import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper

class ResponseWrapper extends HttpServletResponseWrapper {
	private int ptStatusCode

	def ResponseWrapper(HttpServletResponse response) {
		super(response)
	}

	int getPTStatus() {
		ptStatusCode
	}

	@Override
	void sendError(int sc, String msg) {
		this.ptStatusCode = sc
		super.sendError(sc, msg)
	}

	@Override
	void sendError(int sc) {
		this.ptStatusCode = sc
		super.sendError(sc)
	}

	@Override
	void setStatus(int sc) {
		this.ptStatusCode = sc
		super.setStatus(sc)
	}

	@Override
	void setStatus(int sc, String sm) {
		this.ptStatusCode = sc
		super.setStatus(sc, sm)
	}
}
