package us.paperlesstech

import org.apache.shiro.subject.Subject

import us.paperlesstech.nimble.Profile
import us.paperlesstech.nimble.User
import spock.lang.Specification
import grails.test.mixin.TestFor
import grails.test.mixin.Mock

@TestFor(PreferenceService)
@Mock([Preference, User, Profile])
class PreferenceServiceSpec extends Specification {
	PreferenceService service
	AuthService authService = Mock()
	Subject subject = Mock()

	def setup() {
		service = new PreferenceService()
		service.authService = authService
	}

	def "setPreference should throw an Assertion when the current User can't edit the passed in User"() {
		given:
		def user = UnitTestHelper.createUser()
		1 * authService.getAuthenticatedSubject() >> subject
		1 * subject.isPermitted(_) >> false
		when:
		service.setPreference(user, "key", "value")
		then:
		thrown AssertionError
	}

	def "getPreference should throw an Assertion when the current User can't edit the passed in User"() {
		given:
		def user = UnitTestHelper.createUser()
		1 * authService.getAuthenticatedSubject() >> subject
		1 * subject.isPermitted(_) >> false
		when:
		service.getPreference(user, "key")
		then:
		thrown AssertionError
	}

	def "getPreference should return the value associated with a key"() {
		given:
		def user = UnitTestHelper.createUser()
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
		def user = UnitTestHelper.createUser()
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
		def user = UnitTestHelper.createUser()
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
		def user = UnitTestHelper.createUser()
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
		def user = UnitTestHelper.createUser()
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
