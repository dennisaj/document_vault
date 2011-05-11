package us.paperlesstech

import grails.converters.JSON
import us.paperlesstech.handlers.Handler

class SignatureCodeController {
	def activityLogService
	def handlerChain
	def signatureCodeService

	def done = {
	}

	def downloadImage = {
		if (signatureCodeService.verifySignatureCode(params.id, session.signatureCode)) {
			def document = Document.get(params.id)
			def (filename, data, contentType) = handlerChain.retrievePreview(document: document, documentData: document.files.first(), page: params.pageNumber?.toInteger() ?: 1)
			response.setContentType(contentType)
			response.setContentLength(data.length)
			response.getOutputStream().write(data)

			response.status = 404
		}
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
				handlerChain.sign(document: document, documentData: document.files.first(), signatures:signatures)

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
			def d = Document.get(params.id.toInteger())
			assert d

			render(d.previewImageAsMap(params.pageNumber.toInteger()) as JSON)
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

			signatures[params.pageNumber] = JSON.parse(params.lines)
			session.signatures[params.id] = signatures

			render ([status:"success"] as JSON)
		}
	}
}
