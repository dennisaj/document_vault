package us.paperlesstech

import org.apache.shiro.SecurityUtils

import us.paperlesstech.auth.CodeToken
import us.paperlesstech.nimble.User

class CodeController {
	static def allowedMethods = [index: "GET"]

	def authService
	def userService

	def index = {
		def codeToken = new CodeToken(code:params.code)
		assert codeToken.code

		try {
			def party = Party.findByCode(codeToken.code)
			assert party

			// If the user is a generated user, log them in automatically.
			if (party.signator.roles?.any { it.name == User.SIGNATOR_USER_ROLE }) {
				authService.login(codeToken)
				userService.createLoginRecord(request)
			}

			switch (party.documentPermission) {
				case DocumentPermission.Sign:
					redirect(controller:"document", action:"sign", params:[documentId:party.document.id])
					return
				case DocumentPermission.View:
					redirect(controller:"document", action:"show", params:[documentId:party.document.id])
					return
			}
		} catch (Exception e) {
			log.error "Error authenticating code $codeToken.code"
		}

		response.status = 403
		redirect controller:"auth", action:"unauthorized"
	}
}
