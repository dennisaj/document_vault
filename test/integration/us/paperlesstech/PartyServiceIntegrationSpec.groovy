package us.paperlesstech

import grails.plugin.spock.*
import grails.plugins.nimble.core.Group
import grails.plugins.nimble.core.Permission
import spock.lang.*

class PartyServiceIntegrationSpec extends IntegrationSpec {
	def partyService
	def fileData
	def fileService
	AuthService authServiceMock = Mock()

	def setup() {
		partyService.authServiceProxy = authServiceMock
		fileData = fileService.createDocumentData(mimeType: MimeType.PDF, bytes: new byte[1], dateCreated: new Date())
	}

	static def getGroup() {
		def g = new Group(name: "test " + Math.random())
		g.save()
		g
	}

	def getDocument() {
		def d = new Document()
		d.addToFiles(fileData)
		d.group = group
		d
	}

	def "removeParty should remove the party and the associated permission"() {
		given:
			def document = getDocument()
			document.save(failOnError:true)
			def user = new User(username:"user", profile:new Profile())
			user.save()

			def permission = new Permission(owner:user, type:Permission.defaultPerm, target:"document:sign:*:${document.id}", managed:true)
			user.addToPermissions(permission)
			permission.save(failOnError:true)
			user.save(failOnError:true)

			def party = new Party(document:document, signator:user, color:PartyColor.Red, documentPermission:DocumentPermission.Sign)
			document.addToParties(party)
			def highlight = new Highlight(party:party, pageNumber:1)
			party.highlights = [highlight]
			party.save(failOnError:true)
			document.save(failOnError:true)

			1 * authServiceMock.canGetSigned(document) >> true
		when:
			def savedParty = partyService.removeParty(party)
		then:
			Party.count() == 0
			document.parties.empty
			user.permissions.empty
	}
}
