package us.paperlesstech

import grails.plugin.spock.UnitSpec
import groovy.mock.interceptor.MockFor

class ActivityLogServiceSpec extends UnitSpec {
	AuthService authService = Mock()
	def service = new ActivityLogService()
	RequestService request = Mock()
	def activityMock

	def setup() {
		service.authService = authService
		service.requestService = request
		activityMock = mockFor(ActivityLog.class)
	}

	def "add log should call methods off the request service"() {
		activityMock.demand.setUserAgent() {userAgent-> }
		activityMock.demand.setIp() {ip-> }
		activityMock.demand.setUser() {currentUser-> }
		activityMock.demand.setParams() {params->}
		activityMock.demand.setUri() {uri->}
		activityMock.demand.save() {->}
		when: "Try to save"
		def activityLog = service.addLog(params)
		activityMock.verify()

		then:
		1 * request.getHeader("User-Agent") >> userAgent
		1 * request.getRemoteAddr() >> ip
		1 * request.getRequestURI() >> uri
		1 * authService.authenticatedUser >> currentUser

		where:
		userAgent = "FF"
		ip = "127.0.0.1"
		currentUser = new User()
		uri = "/document_vault/document/index"
		params = [param1:"1", param2:"2"]
	}
}
