package us.paperlesstech.nimble

import grails.plugin.spock.UnitSpec

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.commons.GrailsApplication

import us.paperlesstech.helpers.InstanceGenerator

class UserServiceSpec extends UnitSpec {
	def role
	UserService service
	GrailsApplication grailsApplication = Mock()
	RoleService roleService = Mock()

	def setup() {
		mockLogging(UserService)
		service = new UserService()
		service.grailsApplication = grailsApplication
		service.roleService = roleService

		def config = new ConfigObject()
		config.nimble.passwords.minlength = 8
		ConfigurationHolder.config = config

		grailsApplication.metaClass.getConfig = {-> config }

		role = new Role(name: User.SIGNATOR_USER_ROLE, description: User.SIGNATOR_USER_ROLE)
		mockDomain(Role, [role])
	}

	def "createUser should add the SIGNATOR_USER_ROLE when addSignatorRole is true"() {
		given:
		mockDomain(User)
		mockDomain(Profile)
		def user = new User()
		def profile = new Profile()
		def ig = mockFor(InstanceGenerator)

		ig.demand.static.user {-> user}
		ig.demand.static.profile {-> profile}
		roleService.addMember(user, role) >> {
			user.addToRoles(role)
			user
		}
		service.metaClass.createUser = { User u ->
			u
		}
		when:
		def signator = service.createUser(fullName: "fullName", email: "email@email.com", username: "username", addSignatorRole: addSignatorRole)
		then:
		if (addSignatorRole) {
			assert signator.roles?.contains(role) == addSignatorRole
		} else {
			assert !signator.roles?.contains(role)
		}

		where:
		addSignatorRole << [false, true]
	}

	def "createUser should return errors when save fails"() {
		given:
		mockDomain(User)
		mockDomain(Profile)
		def user = new User()
		def profile = new Profile()
		def ig = mockFor(InstanceGenerator)

		ig.demand.static.user {-> user}
		ig.demand.static.profile {-> profile}
		service.metaClass.createUser = { User u ->
			u.errors.reject("nope")
			u
		}
		when:
		def signator = service.createUser(fullName: "fullName", email: "email@email.com", username: "username")
		then:
		signator.hasErrors()
	}
}
