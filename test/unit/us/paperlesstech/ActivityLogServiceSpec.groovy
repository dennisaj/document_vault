package us.paperlesstech

import grails.plugin.spock.UnitSpec
import groovy.mock.interceptor.MockFor
import grails.converters.JSON

class ActivityLogServiceSpec extends UnitSpec {
	AuthService authService = Mock()
	def service = new ActivityLogService()
	RequestService request = Mock()
	def activityMock

	def setup() {
		service.authServiceProxy = authService
		service.requestService = request
		activityMock = mockFor(ActivityLog.class)
	}

	def "add log should call methods off the request service"() {
		mockDomain(ActivityLog)
		when: "Try to save"
		def activityLog = service.addLog(controller, action, status, params)

		then:
		1 * request.getHeader("User-Agent") >> userAgent
		1 * request.getRemoteAddr() >> ip
		1 * request.getRequestURI() >> uri
		1 * authService.authenticatedUser >> currentUser
		activityLog.action == "document:index"
		activityLog.document == "4"
		activityLog.ip == ip
		activityLog.pageNumber == "2"
		activityLog.params == [param1:"1", param2:"2"].toString()
		activityLog.status == status
		activityLog.user == currentUser
		activityLog.userAgent == userAgent
		activityLog.uri == uri

		where:
		action = null
		controller = "document"
		status = 200
		userAgent = "FF"
		ip = "127.0.0.1"
		currentUser = new User()
		uri = "/document_vault/document/index"
		params = [param1:"1", param2:"2", documentId: "4", pageNumber: "2", lines: "blah"]
	}
}
