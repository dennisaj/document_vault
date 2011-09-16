package us.paperlesstech

import grails.plugin.spock.ControllerSpec

import org.apache.shiro.subject.SimplePrincipalCollection
import org.apache.shiro.subject.Subject

import us.paperlesstech.nimble.User

class RunAsControllerSpec extends ControllerSpec {
	AuthService authService = Mock()
	Subject subject = Mock()
	def user = new User(id:1)

	def setup() {
		controller.authService = authService
		mockDomain(User, [user])
	}

	def "runas throws an assertion error when no userid is passed in"() {
		when:
		controller.runas()
		then:
		thrown(AssertionError)
	}

	def "runas should call runAs when a valid id is passed in"() {
		when:
		controller.params.userId = 1
		controller.runas()
		then:
		1 * authService.authenticatedSubject >> subject
		1 * subject.runAs(new SimplePrincipalCollection(user.id, 'localized'))
	}

	def "runas should use the user's realm when it is set"() {
		given:
		user.realm = 'blah'
		when:
		controller.params.userId = 1
		controller.runas()
		then:
		1 * authService.authenticatedSubject >> subject
		1 * subject.runAs(new SimplePrincipalCollection(user.id, 'blah'))
	}

	def "release should call releaseRunAs"() {
		when:
		controller.release()

		then:
		1 * authService.authenticatedSubject >> subject
		1 * subject.releaseRunAs() >> new SimplePrincipalCollection()
	}

	def "afterInterceptor should redirect to the targetUri when it is set"() {
		when:
		controller.params.targetUri = targetUri
		controller.afterInterceptor()

		then:
		controller.redirectArgs.uri == targetUri
		where:
		targetUri = '/'
	}

	def "afterInterceptor should redirect to the document index"() {
		when:
		controller.params.targetUri = null
		controller.afterInterceptor()

		then:
		controller.redirectArgs.controller == 'document'
		controller.redirectArgs.action == 'index'
	}
}
