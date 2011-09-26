package us.paperlesstech

import grails.converters.JSON
import grails.plugin.spock.ControllerSpec

class PartyControllerSpec extends ControllerSpec {
	PartyService partyService = Mock()

	def document1 = new Document(id:1)
	def document2 = new Document(id:2)
	def party1 = new Party(id:1, document:document1)
	def party2 = new Party(id:2, document:document1)

	def setup() {
		controller.partyService = partyService

		mockDomain(Party,[party1, party2])
		mockDomain(Document, [document1, document2])
	}

	def "addParty should throw an AssertionError when given an invalid documentId"() {
		given:
		controller.params.documentId = null
		when:
		controller.addParty()
		then:
		thrown(AssertionError)
	}

	def "addParty should return a filled in model when given valid data"() {
		given:
		controller.params.documentId = '1'
		when:
		controller.addParty()
		then:
		controller.renderArgs.template == 'party'
		controller.renderArgs.model.document == document1
		controller.renderArgs.model.colors == PartyColor.values()
		controller.renderArgs.model.permissions == Party.allowedPermissions
		controller.renderArgs.model.party
	}

	def "removeParty should throw an AssertionError when given an invalid partyId"() {
		given:
		controller.params.partyId = partyId
		controller.params.documentId = null
		when:
		controller.removeParty()
		then:
		thrown(AssertionError)
		where:
		partyId << [null, '3']
	}

	def "removeParty should throw an AssertionError when given an invalid documentId"() {
		given:
		controller.params.partyId = '1'
		controller.params.documentId = documentId
		when:
		controller.removeParty()
		then:
		thrown(AssertionError)
		where:
		documentId << [null, '2']
	}

	def "removeParty should call partyService's removeParty when given valid input"() {
		given:
		controller.params.partyId = '1'
		controller.params.documentId = '1'
		1 * partyService.removeParty(party1)
		when:
		controller.removeParty()
		then:
		JSON.parse(mockResponse.contentAsString).status == 'success'
	}

	def "submitSignatures should throw an AssertionError when given an invalid documentId"() {
		given:
		controller.params.lines = null
		controller.params.documentId = null
		when:
		controller.submitSignatures()
		then:
		thrown(AssertionError)
	}

	def "submitSignatures should set flash yellow when given invalid lines"() {
		given:
		controller.metaClass.message = { LinkedHashMap arg1 -> 'this is stupid' }
		controller.params.lines = '[]'
		controller.params.documentId = '1'
		when:
		controller.submitSignatures()
		then:
		controller.flash.yellow
	}

	def "submitSignatures should call partyService's cursiveSign and set flash green when given valid lines"() {
		given:
		controller.metaClass.message = { LinkedHashMap arg1 -> 'this is stupid' }
		controller.params.lines = '{1:"lines"}'
		controller.params.documentId = '1'
		1 * partyService.cursiveSign(document1, _) >> document1
		when:
		controller.submitSignatures()
		then:
		controller.flash.green
	}

	def "submitSignatures should call partyService's cursiveSign and set flash red when an error is returned"() {
		given:
		controller.metaClass.message = { LinkedHashMap arg1 -> 'this is stupid' }
		controller.params.lines = '{1:"lines"}'
		controller.params.documentId = '1'
		1 * partyService.cursiveSign(document1, _) >> { d, m-> d.errors.rejectValue('id', 'bacause'); d }
		when:
		controller.submitSignatures()
		then:
		controller.flash.red
	}

	def "resend should throw an AssertionError when given an invalid documentId"() {
		given:
		controller.params.partyId = '1'
		controller.params.documentId = documentId
		when:
		controller.resend()
		then:
		thrown(AssertionError)
		where:
		documentId << [null, '2']
	}

	def "resend should throw an AssertionError when given an invalid partyId"() {
		given:
		controller.params.partyId = partyId
		controller.params.documentId = '1'
		when:
		controller.resend()
		then:
		thrown(AssertionError)
		where:
		partyId << [null, '3']
	}

	def "resend should call partyService's sendCode when given valid input"() {
		given:
		controller.params.partyId = '1'
		controller.params.documentId = '1'
		1 * partyService.sendCode(party1)
		when:
		controller.resend()
		then:
		JSON.parse(mockResponse.contentAsString).status == 'success'
	}

	def "submitParties should throw an AssertionError when given an invalid documentId"() {
		given:
		controller.params.parties = null
		controller.params.documentId = documentId
		when:
		controller.submitParties()
		then:
		thrown(AssertionError)
		where:
		documentId << [null, '3']
	}

	def "submitParties should return a filled in model when given valid data"() {
		given:
		controller.params.documentId = '1'
		controller.params.parties = '["parties"]'
		1 * partyService.submitParties(document1, _) >> outParties
		when:
		controller.submitParties()
		then:
		controller.renderArgs.template == 'parties'
		controller.renderArgs.model.document == document1
		controller.renderArgs.model.colors == PartyColor.values()
		controller.renderArgs.model.permissions == Party.allowedPermissions
		controller.renderArgs.model.parties == outParties
		where:
		outParties = ['outParties']
	}
}