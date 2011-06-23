package us.paperlesstech

import grails.converters.JSON

import org.apache.shiro.SecurityUtils

import us.paperlesstech.auth.CodeToken

class CodeController {
	def allowedMethods = [index: "GET"]
	def userService

	def index = {
		def codeToken = new CodeToken(code:params.code)
		assert codeToken.code

		try {
			SecurityUtils.subject.login(codeToken)
			userService.createLoginRecord(request)
			def party = Party.findByCode(codeToken.code)

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
