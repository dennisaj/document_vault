package us.paperlesstech

import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import org.apache.shiro.subject.SimplePrincipalCollection
import org.apache.shiro.subject.Subject

import spock.lang.Specification
import us.paperlesstech.nimble.User

@TestFor(RunAsController)
@Mock([User])
class RunAsControllerSpec extends Specification {
	AuthService authService = Mock()
	NotificationService notificationService = Mock()
	Subject subject = Mock()

	def user

	def setup() {
		controller.authService = authService
		controller.notificationService = notificationService

		user = UnitTestHelper.createUser()
	}

	def "runas throws an assertion error when no userid is passed in"() {
		when:
		controller.runas()
		then:
		thrown(AssertionError)
	}

	def "runas should call runAs when a valid id is passed in"() {
		when:
		controller.runas(1L)
		then:
		1 * authService.authenticatedSubject >> subject
		1 * subject.runAs(new SimplePrincipalCollection(user.id, 'localized'))
	}

	def "runas should use the user's realm when it is set"() {
		given:
		user.realm = 'blah'
		when:
		controller.runas(1L)
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

	def "afterInterceptor should return the targetUri in the output when it is set, otherwise it should return slash"() {
		when:
		controller.params.targetUri = targetUri
		controller.afterInterceptor()
		def result = JSON.parse(response.contentAsString)
		then:
		1 * notificationService.success(_)
		result.uri == expected
		where:
		targetUri << [null,  '/some/url']
		expected << ['/', '/some/url']
	}
}
