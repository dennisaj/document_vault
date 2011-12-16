package us.paperlesstech

import us.paperlesstech.nimble.Permission

class PartyServiceIntegrationSpec extends AbstractMultiTenantIntegrationSpec {
	def partyService
	AuthService authService

	def setup() {
		authService = Mock(AuthService)
		partyService.authService = authService
	}

	def "removeParty should remove the party and the associated permission"() {
		given:
		def document = createDocument(authService: authService)
		def user = createUser()

		def permission = new Permission(owner: user, type: Permission.defaultPerm, target: "document:sign:*:${document.id}", managed: true)
		user.addToPermissions(permission)
		permission.save(failOnError: true)
		user.save(failOnError: true)

		def party = new Party(document: document, signator: user, color: PartyColor.Red, documentPermission: DocumentPermission.Sign)
		document.addToParties(party)
		def highlight = new Highlight(party: party, pageNumber: 1)
		party.highlights = [highlight]
		party.save(failOnError: true)
		document.save(failOnError: true)

		// I'm not sure why this is required a second time.  Maybe the save above is returning a new copy without
		// authService set.
		document.authService = authService
		Document.authService = authService

		when:
		def savedParty = partyService.removeParty(party)

		then:
		1 * authService.canGetSigned(document) >> true
		authService.authenticatedUser >>> user
		Party.count() == 0
		document.parties.empty
		user.permissions.empty
	}
}
