package us.paperlesstech.handlers

import grails.plugin.spock.IntegrationSpec
import org.apache.shiro.subject.Subject

class BaseHandlerSpec extends IntegrationSpec {
	def authService
	Subject adminSubject = Mock()

	def setup() {
		adminSubject = Mock()
		adminSubject.authenticated >> true
		adminSubject.isPermitted(_) >> true
		authService.testSubject = adminSubject
	}
}
