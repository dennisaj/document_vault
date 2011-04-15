package us.paperlesstech

class SignatureCodeService {

	static transactional = true

	def sendCode(Document document, String email) {
		def signatureCode = getCode(document, email)

		// This method is injected to this class by the Grails Mail plugin
		sendMail {
			to email
			subject "Document Vault Signature Code"
			body (view: "email", model:[code:signatureCode.code])
		}
	}

	SignatureCode getCode(Document document, String email) {
		email = email.toLowerCase()
		def signatureCode = SignatureCode.findByDocumentAndEmail(document, email)
		if (!signatureCode) {
			signatureCode = new SignatureCode(email:email, document:document)
			signatureCode.save(flush:true)
		}

		return signatureCode
	}

	Document getDocument(String code) {
		if (code) {
			def signatureCode = SignatureCode.findByCode(code)
			return signatureCode?.document
		}

		return null
	}
	
	boolean verifySignatureCode (documentId, signatureCode) {
		def document = getDocument(signatureCode)

		if (document && documentId == document.id.toString()) {
			return true
		}

		return false
	}
}
