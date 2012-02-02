package us.paperlesstech

import grails.converters.JSON


import us.paperlesstech.helpers.ShiroHelpers

class PartyController {
	static def allowedMethods = [addParty: "POST", image: "POST", removeParty: "POST", resend: "POST",
			submitParties: "POST", submitSignatures: "POST", emailDocument: "POST"]

	def authService
	def highlightService
	def notificationService
	def partyService

	def remove(Long documentId, Long partyId) {
		def party = Party.get(partyId)
		assert party
		assert party.document.id == documentId

		partyService.removeParty(party)

		render([notification:notificationService.success('document-vault.api.party.removeParty.success', [partyId, documentId])] as JSON)
	}

	def codeSign(String documentId, String signatures) {
		def party = Party.findByCode(documentId)
		assert party

		ShiroHelpers.runas(party.signator) {
			this.cursiveSign(party.document.id, signatures)
		}
	}

	def cursiveSign(Long documentId, String signatures) {
		def document = Document.get(documentId)
		assert document

		def signatureList = JSON.parse(signatures).findAll { it.value }

		def notification

		if (signatureList) {
			document = partyService.cursiveSign(document, signatureList)
			if (!document.hasErrors()) {
				notification = notificationService.success('document-vault.api.party.submitSignatures.success', [document.id])
			} else {
				//TODO: Add errors to JSON
				notification = notificationService.error('document-vault.api.party.submitSignatures.error.failure', [document.id])
			}
		} else {
			notification = notificationService.info('document-vault.api.party.submitSignatures.info.nosignatures', [document.id])
		}

		render([notification:notification] as JSON)
	}

	def codeClickWrap(String documentId, String highlights) {
		def party = Party.findByCode(documentId)
		assert party

		ShiroHelpers.runas(party.signator) {
			this.clickWrap(party.document.id, highlights)
		}
	}

	def clickWrap(Long documentId, String highlights) {
		def document = Document.get(documentId)
		assert document

		Map highlightMap = JSON.parse(highlights)

		def updatedHighlights = partyService.clickWrap(document, highlightMap)

		render([notification:notificationService.success('document-vault.api.party.clickWrap.success', [documentId])] as JSON)
	}

	def submitParties(Long documentId, String parties) {
		def document = Document.get(documentId)
		assert document

		def inParties = JSON.parse(parties)

		def outParties = partyService.submitParties(document, inParties)

		render([
			notification:notificationService.success('document-vault.api.party.submitParties.success', [outParties.size()]),
			parties:outParties*.asMap()
		] as JSON)
	}

	def resend(Long documentId, Long partyId) {
		def party = Party.get(partyId)
		assert party
		assert party.document.id == documentId

		partyService.sendCode(party)

		render([notification:notificationService.success('document-vault.api.party.resend.success', [party.signator.profile.email, documentId])] as JSON)
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
