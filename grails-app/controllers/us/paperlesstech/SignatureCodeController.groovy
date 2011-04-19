package us.paperlesstech

import grails.converters.JSON

class SignatureCodeController {

	def activityLogService
	def documentService
	def signatureCodeService

	def done = {
	}

	def error = {
	}

	def finish = {
		if (signatureCodeService.verifySignatureCode(params.id, session.signatureCode)) {
			def document = signatureCodeService.getDocument(session.signatureCode)
			if (document) {
				def signatures = session.signatures.get(document.id.toString()).findAll {it.value}
				
				def notes = "Signed using code " + session.signatureCode
				activityLogService.addSignLog(document, signatures, notes)
				documentService.signDocument(document, session.signatures.get(document.id.toString()))
	
				document.signed = true
				document.save()
	
				flash.green = "Signature saved"
				render ([status:"success"] as JSON)
			}
		}

		render ([status:"error"] as JSON)
	}

	def image = {
		if (signatureCodeService.verifySignatureCode(params.id, session.signatureCode)) {
			render (documentService.getImageDataAsMap(params.id, params.pageNumber.toInteger()) as JSON)
		}

		render ([status:"error"] as JSON)
	}

	def index = {
		session.signatureCode = params.code
		def document = signatureCodeService.getDocument(session.signatureCode)

		if (document) {
			render ([view:"index", model:[document: document]])
		} else {
			redirect([action: error])
		}
	}

	def send = {
		def document = Document.get(params.documentId)
		def email = params.email

		if (document && email) {
			activityLogService.addEmailLog(document, "This document was sent to " + email)
			signatureCodeService.sendCode document, email

			render([status:"success"] as JSON)
		}

		render([status:"error"] as JSON)
	}

	def sign = {
		if (signatureCodeService.verifySignatureCode(params.id, session.signatureCode)) {
			if (!session.signatures) {
				session.signatures = [:]
			}

			def signatures = session.signatures.get(params.id, [:])
			
			if (documentService.saveSignatureToMap(signatures, params.pageNumber, params.imageData)) {
				session.signatures[params.id] = signatures
				render ([status:"success"] as JSON)
			} else {
				render ([status:"error"] as JSON)
			}
		}
	}
}
