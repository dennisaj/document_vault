package us.paperlesstech

import grails.converters.JSON

import org.apache.shiro.subject.SimplePrincipalCollection

import us.paperlesstech.nimble.User

class RunAsController {
	static def allowedMethods = [runas:'POST', release:'GET']

	def authService
	def notificationService

	def afterInterceptor = {
		render([notification:notificationService.success('message'), uri:params.targetUri?:'/'] as JSON)
	}

	def runas = {
		def user = User.get(params.long('userId'))
		assert user

		authService.authenticatedSubject.runAs(new SimplePrincipalCollection(user.id, user.realm?:'localized'))
	}

	def release = {
		authService.authenticatedSubject.releaseRunAs()
	}
}
