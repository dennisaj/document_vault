package us.paperlesstech

import us.paperlesstech.helpers.ShiroHelpers

class CodeController extends DocumentController {
	def beforeInterceptor = [action:this.&wrapper]

	def wrapper() {
		def party = Party.findByCode(params.documentId)
		assert party
		params.documentId = party.document.id

		ShiroHelpers.runas(party.signator) {
			this."${params.action}"()
		}

		return false
	}
}
