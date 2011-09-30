package us.paperlesstech.handlers

import grails.plugin.spock.IntegrationSpec

import org.apache.shiro.subject.Subject

import spock.lang.Shared
import us.paperlesstech.nimble.Profile
import us.paperlesstech.nimble.User

class BaseHandlerSpec extends IntegrationSpec {
	static User user

	def authService
	Subject adminSubject = Mock()

	def setupSpec() {
		if (!user) {
			user = new User(profile:new Profile(), username:"name").save(flush:true)
		}
	}

	def setup() {
		adminSubject = Mock()
		adminSubject.authenticated >> true
		adminSubject.remembered >> true
		adminSubject.isPermitted(_) >> true
		authService.metaClass.isLoggedIn = {
			true
		}
		authService.metaClass.getAuthenticatedUser = {
			user
		}
		authService.testSubject = adminSubject
	}
}
