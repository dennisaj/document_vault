package us.paperlesstech

import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.Profile
import us.paperlesstech.nimble.User

@TestFor(PartyController)
@Mock([Party, Document, DocumentData, PreviewImage, Group, User])
class PartyControllerSpec extends Specification {
	NotificationService notificationService = Mock()
	PartyService partyService = Mock()


	def setup() {
		controller.notificationService = notificationService
		controller.partyService = partyService

		def document1 = UnitTestHelper.createDocument()
		def document2 = UnitTestHelper.createDocument()

		new Party(id:1, document:document1, documentPermission:DocumentPermission.Sign, signator:UnitTestHelper.createUser(), highlights:[new Highlight()]).save(failOnError: true, flush: true)
		new Party(id:2, document:document1, documentPermission:DocumentPermission.Sign, signator:UnitTestHelper.createUser(), highlights:[new Highlight()]).save(failOnError: true, flush: true)
	}

	def "addParty should throw an AssertionError when given an invalid documentId"() {
		when:
		controller.addParty(null)
		then:
		thrown(AssertionError)
	}

	def "addParty should return a filled in model when given valid data"() {
		when:
		controller.addParty(1L)
		def results = JSON.parse(response.contentAsString)
		then:
		1 * notificationService.success(_)
		results.document.id == 1
		results.colors == PartyColor.values()*.name()
		results.permissions == Party.allowedPermissions*.name()
	}

	def "removeParty should throw an AssertionError when given an invalid partyId"() {
		when:
		controller.removeParty(null, partyId)
		then:
		thrown(AssertionError)
		where:
		partyId << [null, 1100L]
	}

	def "removeParty should throw an AssertionError when given an invalid documentId"() {
		when:
		controller.removeParty(documentId, 1L)
		then:
		thrown(AssertionError)
		where:
		documentId << [null, 2L]
	}

	def "removeParty should call partyService's removeParty when given valid input"() {
		when:
		controller.removeParty(documentId, partyId)
		then:
		1 * partyService.removeParty(Party.get(partyId))
		1 * notificationService.success(_, [documentId, partyId])
		where:
		documentId << 1L
		partyId << 1L
	}

	def "submitSignatures should throw an AssertionError when given an invalid documentId"() {
		when:
		controller.submitSignatures(null, null)
		then:
		thrown(AssertionError)
	}

	def "submitSignatures should set flash yellow when given invalid signatures"() {
		when:
		controller.submitSignatures(1L, '[]')
		then:
		controller.flash.yellow
	}

	def "submitSignatures should call partyService's cursiveSign and set flash green when given valid signatures"() {
		given:
		def document1 = Document.get(1L)
		when:
		controller.submitSignatures(document1.id, '{1:{lines:"lines"}}')
		then:
		1 * partyService.cursiveSign(document1, _) >> document1
		controller.flash.green
	}

	def "submitSignatures should call partyService's cursiveSign and set flash red when an error is returned"() {
		given:
		def document1 = Document.get(1L)
		when:
		controller.submitSignatures(document1.id, '{1:{lines:"lines"}}')
		then:
		1 * partyService.cursiveSign(document1, _) >> { d, m-> d.errors.rejectValue('id', 'because'); d }
		controller.flash.red
	}

	def "resend should throw an AssertionError when given an invalid documentId"() {
		when:
		controller.resend(documentId, 1L)
		then:
		thrown(AssertionError)
		where:
		documentId << [null, 2L]
	}

	def "resend should throw an AssertionError when given an invalid partyId"() {
		when:
		controller.resend(1L, partyId)
		then:
		thrown(AssertionError)
		where:
		partyId << [null, 1100L]
	}

	def "resend should call partyService's sendCode when given valid input"() {
		when:
		controller.resend(1L, 1L)
		then:
		1 * partyService.sendCode(Party.get(1L))
		JSON.parse(response.contentAsString).status == 'success'
	}

	def "submitParties should throw an AssertionError when given an invalid documentId"() {
		when:
		controller.submitParties(documentId, null)
		then:
		thrown(AssertionError)
		where:
		documentId << [null, 3L]
	}

	def "submitParties should return a filled in model when given valid data"() {
		given:
		def document1 = Document.get(1L)
		when:
		controller.submitParties(document1.id, '["parties"]')
		def results = JSON.parse(response.contentAsString)
		then:
		1 * partyService.submitParties(document1, _) >> outParties
		1 * notificationService.success(_, _)
		results.document.id == document1.id
		where:
		outParties = ['outParties']
	}

	def "email parties throws an error if there is no document or no email address"() {
		when:
		controller.emailDocument(documentId, email)

		then:
		thrown(AssertionError)

		where:
		documentId | email
		null       | null
		1L         | null
		null       | 'test@example.com'
	}

	def "email creates a party if there is not one with the given email address"() {
		given:
		def document1 = Document.get(1L)
		when:
		controller.emailDocument(1L, 'test@example.com')
		def results = JSON.parse(response.contentAsString)

		then:
		1 * partyService.createParty(document1, _) >> Party.get(1L)
		1 * notificationService.success(_, _) >> [message:'message']
		results.notification.message == 'message'
		document1.parties.contains(Party.get(1L))
	}

	def "email resends the code if a party with the email address already exists"() {
		given:
		def party1 = Party.get(1L)
		def party2 = Party.get(2L)
		def document1 = Document.get(1L)
		document1.parties = [party1, party2] as Set
		party1.signator = new User(profile: new Profile(email: 'test@example.co'))
		party2.signator = new User(profile: new Profile(email: 'test@example.com'))

		when:
		controller.emailDocument(1L, 'test@example.com')
		def results = JSON.parse(response.contentAsString)

		then:
		1 * partyService.sendCode(party2) >> party2
		1 * notificationService.success(_, _) >> [message:'message']
		results.notification.message == 'message'
	}

	def "email returns an error if a party can't be created"() {
		given:
		def document2 = Document.get(2L)
		when:
		controller.emailDocument(document2.id, 'test@example.com')
		def results = JSON.parse(response.contentAsString)

		then:
		1 * partyService.createParty(document2, _) >> null
		1 * notificationService.error(_, _) >> [message:'message']
		results.notification.message == 'message'
		!document2.parties
	}

	def "codeSignatures should throw an error given an invalid document code"() {
		when:
		controller.codeSignatures('', '["signatures"]')
		then:
		thrown(AssertionError)
	}
}
