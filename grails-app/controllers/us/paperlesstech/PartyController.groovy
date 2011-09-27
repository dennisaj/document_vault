package us.paperlesstech

import grails.converters.JSON

class PartyController {
	static def allowedMethods = [addParty:"POST", image:"POST", removeParty:"POST", resend:"POST", submitParties:"POST", submitSignatures:"POST"]

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

	def submitSignatures = {
		def document = Document.get(params.long("documentId"))
		assert document

		def signatures = JSON.parse(params.lines).findAll { it.value }

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
}
