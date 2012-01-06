package us.paperlesstech

import grails.converters.JSON

import org.apache.shiro.subject.SimplePrincipalCollection

import us.paperlesstech.nimble.User

class RunAsController {
	static def allowedMethods = [runas:'POST', release:'GET']

	def authService
	def notificationService

	def afterInterceptor = {
		render([notification:notificationService.success('document-vault.api.runas.success'), uri:params.targetUri?:'/'] as JSON)
	}

	def runas(Long userId) {
		def user = User.get(userId)
		assert user

		authService.authenticatedSubject.runAs(new SimplePrincipalCollection(user.id, user.realm?:'localized'))
	}

	def release() {
		authService.authenticatedSubject.releaseRunAs()
	}
}
