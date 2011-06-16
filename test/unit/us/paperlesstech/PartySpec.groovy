package us.paperlesstech

import spock.lang.*
import grails.plugin.spock.*

class PartySpec extends UnitSpec {
	@Shared
	def notAllowedPermissions = ((DocumentPermission.values() - Party.allowedPermissions) as List)

	def setup() {
		mockForConstraintsTests(Party)
	}

	def "past expiration dates should not be allowed"() {
		when:
			def party = new Party(expiration:new Date()-1)
			def v = party.validate()
		then:
			!v
			party.errors['expiration'] == 'validator.pastdate'
	}

	def "null expiration dates should pass the custom validator"() {
		when:
			def party = new Party(expiration:null)
			def v = party.validate()
		then:
			!party.errors['expiration']
	}

	def "future expiration dates should pass the custom validator"() {
		when:
			def party = new Party(expiration:new Date()+1)
			def v = party.validate()
		then:
			!party.errors['expiration']
	}

	def "only allowedPermissions should validate"() {
		when:
			def party = new Party(documentPermission:perm)
			party.validate()
		then:
			party.errors['documentPermission'] == expected
		where:
			perm << Party.allowedPermissions + notAllowedPermissions
			expected << ([null] * Party.allowedPermissions.size() + ['inList'] * notAllowedPermissions.size())
	}
}
