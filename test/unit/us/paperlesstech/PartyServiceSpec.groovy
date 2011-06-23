package us.paperlesstech

import grails.plugin.spock.*
import grails.plugins.nimble.InstanceGenerator
import grails.plugins.nimble.core.Group
import grails.plugins.nimble.core.Permission
import grails.plugins.nimble.core.PermissionService
import grails.plugins.nimble.core.Role
import grails.plugins.nimble.core.RoleService
import grails.plugins.nimble.core.UserService

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.json.JSONObject

import spock.lang.*

class PartyServiceSpec extends UnitSpec {
	def role
	PartyService service
	AuthService authService = Mock()
	GrailsApplication grailsApplication = Mock()
	HighlightService highlightService = new HighlightService()
	PermissionService permissionService = Mock()
	RoleService roleService = Mock()
	UserService userService = Mock()

	def setup() {
		mockLogging(PartyService)
		service = new PartyService()
		service.authService = authService
		service.grailsApplication = grailsApplication
		service.highlightService = highlightService
		service.permissionService = permissionService
		service.roleService = roleService
		service.userService = userService
		service.metaClass.sendMail = {Closure c->
			true
		}
		
		def config = new ConfigObject() 
		config.nimble.passwords.minlength = 8 
		ConfigurationHolder.config = config 

		grailsApplication.metaClass.getConfig = {-> config }

		role = new Role(name:User.SIGNATOR_USER_ROLE, description:User.SIGNATOR_USER_ROLE)
		mockDomain(Role, [role])
	}

	def "test createParty"() {
		given:
			mockDomain(Document)
			mockDomain(Party)
			mockDomain(User)
			def user = new User(id:1)
			def document = new Document(id:1)
			def highlights = [JSONObject.NULL, [[a:[x:10, y:20], b:[x:30, y:40]], JSONObject.NULL]]
			def input = [fullName:"fullName", email:"email@email.com", color:PartyColor.Red.name(), permission:DocumentPermission.Sign.name(), highlights:highlights]

			service.metaClass.getSignator = {String a, String b->
				user
			}

			2 * authService.canGetSigned(_) >> true
			1 * permissionService.createPermission(_,_)
		when:
			def party = service.createParty(document, input)
		then:
			party.document == document
			party.signator == user
			party.color.name() == input.color
			party.documentPermission.name() == input.permission
			party.highlights.size() == highlightService.fromJsonList(party, highlights).size()
	}

	def "createParty should return errors when signator has errors"() {
		given:
			mockDomain(Document)
			mockDomain(Party)
			mockDomain(User)
			def user = new User(id:1)
			def document = new Document(id:1)
			def highlights = [JSONObject.NULL, [[a:[x:10, y:20], b:[x:30, y:40]], JSONObject.NULL]]
			def input = [fullName:"fullName", email:"email@email.com", color:PartyColor.Red.name(), permission:DocumentPermission.Sign.name(), highlights:highlights]

			service.metaClass.getSignator = {String a, String b->
				user.errors.reject("bad")
				user
			}

			1 * authService.canGetSigned(_) >> true
		when:
			def party = service.createParty(document, input)
		then:
			party.signator.hasErrors()
	}

	def "createParty should return errors when color or documentPermission is missing or expiration is malformed"() {
		given:
			mockDomain(Document)
			mockDomain(Party)
			mockDomain(User)
			def user = new User(id:1)
			def document = new Document(id:1)
			def highlights = [JSONObject.NULL, [[a:[x:10, y:20], b:[x:30, y:40]], JSONObject.NULL]]
			def input = [fullName:"fullName", email:"email@email.com", highlights:highlights, expiration:"bad date"]

			service.metaClass.getSignator = {String a, String b->
				user
			}

			1 * authService.canGetSigned(_) >> true
		when:
			def party = service.createParty(document, input)
		then:
			party.errors["color"] == "nullable"
			party.errors["documentPermission"] == "nullable"
			party.errors["expiration"] == "party.expiration.invalidformat"
	}

	def "createParty should throw AssertionError when the user is not authorized"() {
		given:
			1 * authService.canGetSigned(_) >> false
		when:
			service.createParty(new Document(id:1), [:])
		then:
			thrown AssertionError
	}

	def "removeParty should throw AssertionError when the user is not authorized"() {
		given:
			1 * authService.canGetSigned(_) >> false
		when:
			service.removeParty(new Party(id:1))
		then:
			thrown AssertionError
	}

	def "removeParty should throw AssertionError when party is null"() {
		when:
			service.removeParty(null)
		then:
			thrown AssertionError
	}

	def "updateHighlights should throw AssertionError when the user is not authorized"() {
		given:
			1 * authService.canGetSigned(_) >> false
		when:
			service.updateHighlights(new Party(id:1), [])
		then:
			thrown AssertionError
	}

	def "updateHighlights should throw AssertionError when party is null"() {
		when:
			service.updateHighlights(null, [])
		then:
			thrown AssertionError
	}

	def "createUser should add the SIGNATOR_USER_ROLE"() {
		given:
			mockDomain(User)
			mockDomain(Profile)
			def user = new User()
			def profile = new Profile()
			def ig = mockFor(InstanceGenerator)

			ig.demand.static.user {-> user}
			ig.demand.static.profile {-> profile}
			1 * userService.createUser (user) >> { user.save() }
			1 * roleService.addMember (user, role) >> {
				user.addToRoles(role)
				user
			}
		when:
			def signator = service.createUser("fullName", "email@email.com")
		then:
			signator.roles.contains(role)
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
			1 * userService.createUser (user) >> {
				user.errors.reject("nope")
				user
			}
		when:
			def signator = service.createUser("fullName", "email@email.com")
		then:
			signator.hasErrors()
	}

	def "getSignator should return an existing user if the email address is already registered"() {
		given:
			def email = "email@email.com"
			def fullName = "fullName"
			def user = new User(id:1)
			def profile = new Profile()
			profile.fullName = fullName
			profile.email = email
			profile.owner = user
			mockDomain(User)
			mockDomain(Profile, [profile])
		when:
			def signator = service.getSignator(fullName, email)
		then:
			signator == user
	}

	def "getSignator should create a new user if the email address is not already registered"() {
		given:
			mockDomain(User)
			mockDomain(Profile)
			def email = "email@email.com"
			def fullName = "fullName"
			def user = new User(id:1)
			def profile = new Profile()
			profile.fullName = fullName
			profile.email = email
			profile.owner = user
			def ig = mockFor(InstanceGenerator)

			ig.demand.static.user {-> user}
			ig.demand.static.profile {-> profile}
			1 * userService.createUser (user) >> {
				user
			}

			1 * roleService.addMember (user, role) >> {
				user
			}
		when:
			def signator = service.getSignator(fullName, email)
		then:
			signator == user
	}
}
