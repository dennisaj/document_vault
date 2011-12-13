package us.paperlesstech

import grails.plugin.spock.ControllerSpec

import org.apache.shiro.authc.AuthenticationException

import us.paperlesstech.nimble.Role
import us.paperlesstech.nimble.User
import us.paperlesstech.nimble.UserService

class CodeControllerSpec extends ControllerSpec {
	def party = new Party(id:1, code:'code123', document:new Document(id:1))
	def generatedUser = new User(id:1, roles:[new Role(name:User.SIGNATOR_USER_ROLE)])

	def setup() {
		mockDomain(Party, [party])
		mockDomain(User, [generatedUser])
	}
}
