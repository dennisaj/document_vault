package us.paperlesstech.handlers

import grails.plugin.spock.IntegrationSpec

import org.apache.shiro.subject.Subject

import us.paperlesstech.nimble.User

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
		authServiceProxy.metaClass.getAuthenticatedUser = {
			new User(id:1)
		}
		authServiceProxy.testSubject = adminSubject
	}
}
