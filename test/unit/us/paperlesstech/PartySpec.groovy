package us.paperlesstech

import spock.lang.Specification
import spock.lang.Shared

class PartySpec extends Specification {
	@Shared
	Date now = new Date()
	@Shared
	def notAllowedPermissions = ((DocumentPermission.values() - Party.allowedPermissions) as List)

	def setup() {
		mockForConstraintsTests(Party)
	}

	def "past expiration dates should be allowed when id is set"() {
		when:
			def party = new Party(id:1, expiration:new Date()-1)
			def v = party.validate()
		then:
			!party.errors['expiration']
	}

	def "past expiration dates should not be allowed when id is not set"() {
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

	def "test status based on underived Party attributes"() {
		when:
			def party = new Party(params)
			def status = party.status()
		then:
			status == expectedStatus
		where:
			params << [[:], [id:1, viewed:true, sent:false], [id:1, viewed:true, sent:true], [id:1, sent:false], [id:1, viewed:false, sent:true], [id:1, sent:true, viewed:true, rejected:true]]
			expectedStatus << ["document-vault.view.party.status.unsaved",
				"document-vault.view.party.status.viewed",
				"document-vault.view.party.status.viewed",
				"document-vault.view.party.status.unsent",
				"document-vault.view.party.status.sent",
				"document-vault.view.party.status.rejected"]
	}

	def "test status based on derived Party attributes"() {
		when:
			def party = new Party()
			party.metaClass.partiallySigned = {
				params.partiallySigned
			}

			party.metaClass.completelySigned = {
				params.completelySigned
			}
		then:
			party.status() == expectedStatus
		where:
			params << [[completelySigned:false, partiallySigned:true], [completelySigned:true, partiallySigned:false]]
			expectedStatus <<
				["document-vault.view.party.status.partiallysigned",
				"document-vault.view.party.status.signed"]
	}

	def "test partiallySigned"() {
		when:
			def party = new Party(highlights:highlights)
		then:
			party.partiallySigned() == expected
		where:
			highlights << [[new Highlight(accepted:now), new Highlight(accepted:now), new Highlight(accepted:now)],
				[new Highlight(accepted:null), new Highlight(accepted:null), new Highlight(accepted:null)],
				[new Highlight(accepted:null), new Highlight(accepted:now), new Highlight(accepted:null)],
				[]]
			expected << [false, false, true, false]
	}

	def "test completelySigned"() {
		when:
			def party = new Party(highlights:highlights)
		then:
			party.completelySigned() == expected
		where:
			highlights << [[new Highlight(accepted:now), new Highlight(accepted:null), new Highlight(accepted:now)],
				[new Highlight(accepted:null), new Highlight(accepted:null), new Highlight(accepted:null)],
				[new Highlight(accepted:now), new Highlight(accepted:now), new Highlight(accepted:now)],
				[]]
			expected << [false, false, true, false]
	}

	def "test removable"() {
		when:
			def party = new Party(rejected:params.rejected)
			party.metaClass.partiallySigned = {
				params.partiallySigned
			}

			party.metaClass.completelySigned = {
				params.completelySigned
			}
		then:
			party.removable() == expected
		where:
			//!rejected && !partiallySigned() && !completelySigned()
			params << [[completelySigned:false, partiallySigned:false, rejected:false],
				[completelySigned:true, partiallySigned:false, rejected:false],
				[completelySigned:false, partiallySigned:true, rejected:false],
				[completelySigned:false, partiallySigned:false, rejected:true]]
			expected << [true, false, false, false]
	}
}
