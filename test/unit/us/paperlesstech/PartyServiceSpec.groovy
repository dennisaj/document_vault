package us.paperlesstech

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.json.JSONObject

import us.paperlesstech.nimble.PermissionService
import us.paperlesstech.nimble.Profile
import us.paperlesstech.nimble.User
import us.paperlesstech.nimble.UserService
import spock.lang.Specification
import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import us.paperlesstech.nimble.Group

@TestFor(PartyService)
@Mock([Party, User, Profile, ActivityLog, Document, DocumentData, PreviewImage, Group])
class PartyServiceSpec extends Specification {
	PartyService service
	AuthService authService = Mock()
	GrailsApplication grailsApplication = Mock()
	HighlightService highlightService = new HighlightService()
	PermissionService permissionService = Mock()
	UserService userService = Mock()

	def setup() {
		service = new PartyService()
		service.authService = authService
		service.grailsApplication = grailsApplication
		service.highlightService = highlightService
		service.permissionService = permissionService
		service.userService = userService
		service.metaClass.sendMail = {Closure c ->
			true
		}

		def config = new ConfigObject()
		config.nimble.passwords.minlength = 8
		ConfigurationHolder.config = config

		grailsApplication.metaClass.getConfig = {-> config }
	}

	def "test createParty"() {
		given:
		def user = new User(id: 1)
		def document = new Document(id: 1)
		def highlights = [JSONObject.NULL, [[left: 10, top: 20, width: 30, height: 40], JSONObject.NULL]]
		def input = [fullName: "fullName", email: "email@email.com", color: PartyColor.Red.name(), permission: DocumentPermission.Sign.name(), highlights: highlights]

		service.metaClass.getSignator = {String a, String b ->
			user
		}

		2 * authService.canGetSigned(_) >> true
		1 * permissionService.createPermission(_, _)
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
		def user = new User(id: 1)
		def document = new Document(id: 1)
		def highlights = [JSONObject.NULL, [[left: 10, top: 20, width: 30, height: 40], JSONObject.NULL]]
		def input = [fullName: "fullName", email: "email@email.com", color: PartyColor.Red.name(), permission: DocumentPermission.Sign.name(), highlights: highlights]

		service.metaClass.getSignator = {String a, String b ->
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
		def user = new User(id: 1)
		def document = new Document(id: 1)
		def highlights = [JSONObject.NULL, [[left: 10, top: 20, width: 30, height: 40], JSONObject.NULL]]
		def input = [fullName: "fullName", email: "email@email.com", highlights: highlights, expiration: "bad date"]

		service.metaClass.getSignator = {String a, String b ->
			user
		}

		1 * authService.canGetSigned(_) >> true
		when:
		def party = service.createParty(document, input)
		then:
		party.errors["documentPermission"].codes.any { it == "nullable" }
		party.errors["expiration"].codes.any { it == "party.expiration.invalidformat" }
	}

	def "createParty should throw AssertionError when the user is not authorized"() {
		given:
		1 * authService.canGetSigned(_) >> false
		when:
		service.createParty(new Document(id: 1), [:])
		then:
		thrown AssertionError
	}

	def "removeParty should throw AssertionError when the user is not authorized"() {
		given:
		1 * authService.canGetSigned(_) >> false
		when:
		service.removeParty(new Party(id: 1))
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
		service.updateHighlights(new Party(id: 1), [])
		then:
		thrown AssertionError
	}

	def "updateHighlights should throw AssertionError when party is null"() {
		when:
		service.updateHighlights(null, [])
		then:
		thrown AssertionError
	}

	def "getSignator should return an existing user if the email address is already registered"() {
		given:
		def user = UnitTestHelper.createUser()
		user.profile.fullName = fullName
		user.profile.email = email
		user.profile.save(flush: true, failOnError: true)

		when:
		def signator = service.getSignator(fullName, email)

		then:
		signator == user

		where:
		email = "email@email.com"
		fullName = "fullName"
	}

	def "getSignator should create a new user if the email address is not already registered"() {
		given:
		def email = "email@email.com"
		def fullName = "fullName"
		def user = new User(id: 1)
		def map = [:]
		1 * userService.createUser({ map = it}) >> {
			user
		}

		when:
		def signator = service.getSignator(fullName, email)
		then:
		signator == user
		map.fullName == fullName
		map.email == email
		map.username == email
		map.addSignatorRole == true
	}

	def "sendCode calls sendMail and returns the party if it has been sent"() {
		service.metaClass.sendMail = { Closure closure -> assert closure }
		def party = new Party(document: new Document())
		party.sent = true

		when:
		service.sendCode(party) == party

		then:
		1 * authService.canGetSigned(party.document) >> true
	}

	def "sendCode calls sendMail and marks the party as sent"() {
		service.metaClass.sendMail = { Closure closure -> assert closure }
		def party = new Party(document: new Document())
		def savedParty = new Party()
		party.metaClass.save = {-> savedParty }

		when:
		def result = service.sendCode(party)

		then:
		1 * authService.canGetSigned(party.document) >> true
		result == savedParty

	}
}
