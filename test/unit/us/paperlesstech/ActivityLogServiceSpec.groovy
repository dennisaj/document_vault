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
		def activityLog = service.addLog(activityType, document, notes, signatures)

		then:
		1 * request.getHeader("User-Agent") >> userAgent
		1 * request.getRemoteAddr() >> ip
		1 * authenticatedService.authenticatedUser >> currentUser
		activityLog.activityType == activityType
		activityLog.userAgent == userAgent
		activityLog.ip == ip
		activityLog.user == currentUser
		activityLog.pagesAffected == "1,2,3"
		activityLog.document == document
		activityLog.notes == notes

		where:
		activityType = ActivityLog.ActivityType.VIEW
		userAgent = "FF"
		ip = "127.0.0.1"
		currentUser = new User()
		signatures = [1: 1, 2: 2, 3: 3]
		document = new Document()
		notes = "notes"
	}
}
