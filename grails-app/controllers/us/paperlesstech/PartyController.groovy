package us.paperlesstech

import grails.converters.JSON

import java.util.concurrent.Callable

import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.SimplePrincipalCollection
import org.apache.shiro.subject.Subject

class PartyController {
	static def allowedMethods = [addParty: "POST", image: "POST", removeParty: "POST", resend: "POST",
			submitParties: "POST", submitSignatures: "POST", emailDocument: "POST"]

	def notificationService
	def partyService

	def addParty(Long documentId) {
		def document = Document.get(documentId)
		assert document

		render([notification:notificationService.success('document-vault.api.party.addParty.success'), document:document, colors:PartyColor.values()*.name(), permissions:Party.allowedPermissions*.name(), party:new Party()] as JSON)
	}

	def removeParty(Long documentId, Long partyId) {
		def party = Party.get(partyId)
		assert party
		assert party.document.id == documentId

		partyService.removeParty(party)

		render([notification:notificationService.success('document-vault.api.party.removeParty.success', [partyId, documentId])] as JSON)
	}

	def codeSignatures(String documentId, String signatures) {
		def party = Party.findByCode(documentId)
		assert party

		PrincipalCollection principals = new SimplePrincipalCollection(party.signator.id, "localized")
		Subject subject = new Subject.Builder().principals(principals).buildSubject()
		subject.execute({
			this.submitSignatures(party.document.id, signatures)
		} as Callable)
	}

	def submitSignatures(Long documentId, String signatures) {
		def document = Document.get(documentId)
		assert document

		def signatureList = JSON.parse(signatures).findAll { it.value }

		if (signatureList) {
			document = partyService.cursiveSign(document, signatureList)
			if (!document.hasErrors()) {
				flash.green = g.message(code:"document-vault.signature.success", args:[document])
			} else {
				flash.red = g.message(code:"document-vault.signature.error.failure", args:[document])
			}
		} else {
			flash.yellow = g.message(code:"document-vault.signature.error.nosignatures", args:[document])
		}

		render([status:"success"] as JSON)
	}

	def submitParties(Long documentId, String parties) {
		def document = Document.get(documentId)
		assert document

		def inParties = JSON.parse(parties)

		def outParties = partyService.submitParties(document, inParties)

		render([
			notification:notificationService.success('document-vault.api.party.submitParties.success', [outParties.size()]),
			document:document,
			parties:outParties
		] as JSON)
	}

	def resend(Long documentId, Long partyId) {
		def party = Party.get(partyId)
		assert party
		assert party.document.id == documentId

		partyService.sendCode(party)

		render([status:"success"] as JSON)
	}

	def emailDocument(Long documentId, String email) {
		def document = Document.get(documentId)
		assert document
		email = email?.trim()?.toLowerCase()
		assert email

		def party = document.parties.find {
			it.signator.profile.email == email
		}

		if (party) {
			party = partyService.sendCode(party)
		} else {
			party = partyService.createParty(document,
					[email: email, permission: DocumentPermission.View.name()])
		}

		def returnMap = [:]
		if (party && !party.hasErrors()) {
			if (!document.parties.contains(party)) {
				document.addToParties(party)
			}
			document.save()
			returnMap.notification = notificationService.success('document-vault.api.party.email.success', [email])
		} else {
			returnMap.notification = notificationService.error('document-vault.api.party.email.error', [email])
		}

		render returnMap as JSON
	}
}
