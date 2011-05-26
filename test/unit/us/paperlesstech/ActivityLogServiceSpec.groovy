package us.paperlesstech

import grails.plugin.spock.UnitSpec
import grails.plugins.nimble.core.AuthenticatedService

class ActivityLogServiceSpec extends UnitSpec {
	AuthenticatedService authenticatedService = Mock()
	def service = new ActivityLogService()
	RequestService request = Mock()

	def setup() {
		service.authenticatedService = authenticatedService
		service.requestService = request

		mockDomain(ActivityLog)
	}

	def "add log should call methods off the request service"() {
		when: "Try to save"
		def activityLog = service.addLog(params)

		then:
		1 * request.getHeader("User-Agent") >> userAgent
		1 * request.getRemoteAddr() >> ip
		1 * request.getRequestURI() >> uri
		1 * authenticatedService.authenticatedUser >> currentUser
		activityLog.userAgent == userAgent
		activityLog.ip == ip
		activityLog.user == currentUser
		activityLog.uri == uri
		activityLog.params == params.toString()

		where:
		userAgent = "FF"
		ip = "127.0.0.1"
		currentUser = new User()
		uri = "/document_vault/document/index"
		params = [param1:["1"], param2:["2"]]
	}
}
