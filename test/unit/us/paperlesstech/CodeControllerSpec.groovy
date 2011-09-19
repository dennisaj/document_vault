package us.paperlesstech

import grails.plugin.spock.ControllerSpec

import org.apache.shiro.authc.AuthenticationException

import us.paperlesstech.nimble.Role
import us.paperlesstech.nimble.User
import us.paperlesstech.nimble.UserService

class CodeControllerSpec extends ControllerSpec {
	AuthService authService = Mock()
	UserService userService = Mock()
	def party = new Party(id:1, code:'code123', document:new Document(id:1))
	def generatedUser = new User(id:1, roles:[new Role(name:User.SIGNATOR_USER_ROLE)])
	def normalUser = new User(id:2, roles:[new Role(name:'Some Other role')])

	def setup() {
		controller.authService = authService
		controller.userService = userService
		mockDomain(Party, [party])
		mockDomain(User, [generatedUser, normalUser])
	}

	def "index throws AssertionError when no code is given"() {
		when:
		controller.index()
		then:
		thrown(AssertionError)
	}

	def "index throws AssertionError when an invalid party is given"() {
		when:
		controller.params.code = 'bad-code'
		controller.index()
		then:
		thrown(AssertionError)
	}

	def "normal users should not be logged in automatically"() {
		given:
		party.signator = normalUser
		when:
		controller.params.code = 'code123'
		controller.index()
		then:
		0 * authService.login(_)
		0 * userService.createLoginRecord(_)
	}

	def "generated users should be logged in automatically"() {
		given:
		party.signator = generatedUser
		when:
		controller.params.code = 'code123'
		controller.index()
		then:
		1 * authService.login(_)
		1 * userService.createLoginRecord(_)
	}

	def "users should be redirected to the page associated with their respective permission"() {
		given:
		party.signator = generatedUser
		party.documentPermission = perm
		when:
		controller.params.code = 'code123'
		controller.index()
		then:
		controller.redirectArgs.controller == 'document'
		controller.redirectArgs.action == action
		where:
		perm << [DocumentPermission.Sign, DocumentPermission.View]
		action << ['sign', 'show']
	}

	def "exceptions should redirect the user to the unauthorized page"() {
		given:
		party.signator = generatedUser
		1 * authService.login(_) >> { token-> throw new AuthenticationException() }
		when:
		controller.params.code = 'code123'
		controller.index()
		then:
		controller.redirectArgs.controller == 'auth'
		controller.redirectArgs.action == 'unauthorized'
		mockResponse.status == 403
	}
}
