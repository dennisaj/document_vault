package us.paperlesstech

import org.apache.shiro.subject.SimplePrincipalCollection

import us.paperlesstech.nimble.User

class RunAsController {
	static def allowedMethods = [runas:"POST", release:"GET"]
	static def navigation = [[group:'user', action:'release', isVisible: { authService.authenticatedSubject?.isRunAs() }, order:0, title:'Release', params:[targetUri:"/"]]]

	def authService

	def afterInterceptor = {
		if (params.targetUri) {
			redirect(uri:params.targetUri)
		} else {
			redirect(controller:"document", action:"index")
		}
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
