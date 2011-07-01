package us.paperlesstech.handlers

import grails.plugin.spock.IntegrationSpec
import org.apache.shiro.subject.Subject

class BaseHandlerSpec extends IntegrationSpec {
	def authServiceProxy
	Subject adminSubject = Mock()

	def setup() {
		adminSubject = Mock()
		adminSubject.authenticated >> true
		adminSubject.remembered >> true
		adminSubject.isPermitted(_) >> true
		authServiceProxy.metaClass.isLoggedIn = {
			true
		}
		authServiceProxy.testSubject = adminSubject
	}
}
