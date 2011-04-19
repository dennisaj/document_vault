package us.paperlesstech

import grails.plugin.spock.UnitSpec
import grails.plugins.springsecurity.SpringSecurityService

class ActivityLogServiceSpec extends UnitSpec {
	def service = new ActivityLogService()
	SpringSecurityService springSecurityService = Mock()
	RequestService request = Mock()

	def setup() {
		service.springSecurityService = springSecurityService
		service.requestService = request

		mockDomain(ActivityLog)
	}

	def "add log should call methods off the request service"() {
		when: "Try to save"
		def activityLog = service.addLog(activityType, document, notes, signatures)

		then:
		1 * request.getHeader("User-Agent") >> userAgent
		1 * request.getRemoteAddr() >> ip
		1 * springSecurityService.currentUser >> currentUser
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
		signatures = [1:1, 2:2, 3:3]
		document = new Document()
		notes = "notes"
	}
}