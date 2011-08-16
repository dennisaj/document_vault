package us.paperlesstech

import grails.plugin.spock.*

import org.apache.shiro.subject.Subject

import spock.lang.*
import us.paperlesstech.nimble.Profile
import us.paperlesstech.nimble.User

class PreferenceServiceSpec extends UnitSpec {
	PreferenceService service
	AuthService authService = Mock()
	Subject subject = Mock()

	def setup() {
		mockLogging(PreferenceService)
		service = new PreferenceService()
		service.authServiceProxy = authService
	}

	def "setPreference should throw an Assertion when the current User can't edit the passed in User"() {
		given:
		mockDomain(User)
		def user = new User(id: 1)
		1 * authService.getAuthenticatedSubject() >> subject
		1 * subject.isPermitted(_) >> false
		when:
		service.setPreference(user, "key", "value")
		then:
		thrown AssertionError
	}

	def "getPreference should throw an Assertion when the current User can't edit the passed in User"() {
		given:
		mockDomain(User)
		def user = new User(id: 1)
		1 * authService.getAuthenticatedSubject() >> subject
		1 * subject.isPermitted(_) >> false
		when:
		service.getPreference(user, "key")
		then:
		thrown AssertionError
	}

	def "getPreference should return the value associated with a key"() {
		given:
		mockDomain(User)
		mockDomain(Preference)
		def user = new User(id:1)
		def preference = new Preference(key:"key", value:"cheese")
		user.addToPreferences(preference)
		user.save()
		1 * authService.getAuthenticatedSubject() >> subject
		1 * subject.isPermitted(_) >> true
		when:
		def value = service.getPreference(user, "key")
		then:
		value == "cheese"
	}

	def "getPreference should null if no Preference with the given key is found"() {
		given:
		mockDomain(User)
		mockDomain(Preference)
		def user = new User(id:1)
		def preference = new Preference(key:"key", value:"cheese")
		user.addToPreferences(preference)
		user.save()
		1 * authService.getAuthenticatedSubject() >> subject
		1 * subject.isPermitted(_) >> true
		when:
		def value = service.getPreference(user, "bad-key")
		then:
		value == null
	}

	def "setPreference should create a new Preference if no existing Preference with the given key exists"() {
		given:
		mockDomain(User)
		mockDomain(Preference)
		def user = new User(id:1, username:"username", profile:new Profile(id:1))
		user.save()
		1 * authService.getAuthenticatedSubject() >> subject
		1 * subject.isPermitted(_) >> true
		when:
		def result = service.setPreference(user, key, value)
		then:
		result == true
		user.preferences.find { it.key == key }.value == value
		where:
		key << ["key"]
		value << ["cheese"]
	}

	def "setPreference should update an existing Preference"() {
		given:
		mockDomain(User)
		mockDomain(Preference)
		def user = new User(id:1, username:"username", profile:new Profile(id:1))
		def preference = new Preference(key:key, value:"cheese")
		user.addToPreferences(preference)
		user.save()
		1 * authService.getAuthenticatedSubject() >> subject
		1 * subject.isPermitted(_) >> true
		when:
		def result = service.setPreference(user, key, value)
		then:
		result == true
		user.preferences.find { it.key == key }.value == value
		where:
		key << ["key"]
		value << ["goat"]
	}

	def "setPreference should return false if user save fails"() {
		given:
		mockDomain(User)
		mockDomain(Preference)
		def user = new User(id:1, username:"username", profile:new Profile(id:1))
		user.save()
		user.metaClass.save = {
			false
		}
		1 * authService.getAuthenticatedSubject() >> subject
		1 * subject.isPermitted(_) >> true
		when:
		def result = service.setPreference(user, key, value)
		then:
		result == false
		where:
		key << ["key"]
		value << ["goat"]
	}
}
