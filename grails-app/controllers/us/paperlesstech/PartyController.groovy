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

	def addParty = {
		def document = Document.get(params.long("documentId"))
		assert document

		render(template:"party", model:[document:document, colors:PartyColor.values(), permissions:Party.allowedPermissions, party:new Party()])
	}

	def removeParty = {
		def party = Party.get(params.long("partyId"))
		assert party
		assert party.document.id == params.long("documentId")

		partyService.removeParty(party)

		render([status:"success"] as JSON)
	}
	
	def codeSignatures = {
		def party = Party.findByCode(params.documentId)
		assert party
		params.documentId = party.document.id
		
		PrincipalCollection principals = new SimplePrincipalCollection(party.signator.id, "localized")
		Subject subject = new Subject.Builder().principals(principals).buildSubject()
		subject.execute({
			this.submitSignatures()
		} as Callable)
	}

	def submitSignatures = {
		def document = Document.get(params.long("documentId"))
		assert document
		
		def signatures = JSON.parse(params.signatures).findAll { it.value }
		
		if (signatures) {
			document = partyService.cursiveSign(document, signatures)
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

	def submitParties = {
		def document = Document.get(params.long("documentId"))
		assert document

		def inParties = JSON.parse(params.parties)

		def outParties = partyService.submitParties(document, inParties)

		render(template:"parties", model:[colors:PartyColor.values(), document:document, permissions:Party.allowedPermissions, parties:outParties])
	}

	def resend = {
		def party = Party.get(params.long("partyId"))
		assert party
		assert party.document.id == params.long("documentId")

		partyService.sendCode(party)

		render([status:"success"] as JSON)
	}

	def emailDocument = {
		def document = Document.get(params.long("documentId"))
		assert document
		def email = params.email?.trim()?.toLowerCase()
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
