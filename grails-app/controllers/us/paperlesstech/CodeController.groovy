package us.paperlesstech

import us.paperlesstech.auth.CodeToken
import us.paperlesstech.nimble.User

class CodeController {
	static def allowedMethods = [index: "GET"]
	static def allowedPermissions = [DocumentPermission.Sign, DocumentPermission.View] as Set

	def authService
	def requestService
	def userService

	def index = {
		def codeToken = new CodeToken(code: params.code)
		assert codeToken.code
		String url

		try {
			def party = Party.findByCode(codeToken.code)
			assert party

			// If the user is a generated user, log them in automatically.
			if (party.signator.roles?.any { it.name == User.SIGNATOR_USER_ROLE }) {
				authService.login(codeToken)
				userService.createLoginRecord(request)
			}

			if (party.documentPermission in allowedPermissions) {
				def fragment = "${party.document.id},1"
				url = g.createLink(mapping: 'signPage', fragment: fragment, base: requestService.baseAddr)
			}
		} catch (Throwable e) {
			log.error "Error authenticating code $codeToken.code"
		}

		if (!url) {
			url = g.createLink(mapping: 'homePage', base: requestService.baseAddr)
		}
		redirect url: url
	}
}
