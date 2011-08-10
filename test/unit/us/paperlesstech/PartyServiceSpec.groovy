package us.paperlesstech

import grails.plugin.spock.UnitSpec
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.json.JSONObject

import us.paperlesstech.nimble.PermissionService;
import us.paperlesstech.nimble.Profile;
import us.paperlesstech.nimble.User;
import us.paperlesstech.nimble.UserService;

class PartyServiceSpec extends UnitSpec {
	PartyService service
	AuthService authService = Mock()
	GrailsApplication grailsApplication = Mock()
	HighlightService highlightService = new HighlightService()
	PermissionService permissionService = Mock()
	UserService userService = Mock()

	def setup() {
		mockLogging(PartyService)
		service = new PartyService()
		service.authServiceProxy = authService
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
		mockDomain(Document)
		mockDomain(Party)
		mockDomain(User)
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
		mockDomain(Document)
		mockDomain(Party)
		mockDomain(User)
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
		mockDomain(Document)
		mockDomain(Party)
		mockDomain(User)
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
		party.errors["color"] == "nullable"
		party.errors["documentPermission"] == "nullable"
		party.errors["expiration"] == "party.expiration.invalidformat"
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
		def email = "email@email.com"
		def fullName = "fullName"
		def user = new User(id: 1)
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
		def email = "email@email.com"
		def fullName = "fullName"
		def user = new User(id: 1)
		mockDomain(User)
		mockDomain(Profile)
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
}
