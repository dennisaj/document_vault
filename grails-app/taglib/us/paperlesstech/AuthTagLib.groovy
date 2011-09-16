package us.paperlesstech

import org.codehaus.groovy.grails.web.pages.GroovyPage

import us.paperlesstech.nimble.User

class AuthTagLib {
	static final namespace = "pt"

	def authServiceProxy
	def facebookService

	/**
	 *
	 * @return
	 * <code>body()</code> If allowed is true and body is not empty.<br>
	 * <code>true</code> If allowed is true and body is empty.<br>
	 * <code>null</code> If allowed is false
	 */
	protected def outputBody(allowed, body) {
		def _body = (body != GroovyPage.EMPTY_BODY_CLOSURE ? body : { true })
		out << (allowed ? _body() : null)
	}

	def isLoggedIn = { attrs, body ->
		if (authServiceProxy.isLoggedIn()) {
			out << body()
		}
	}

	def username = { attrs, body ->
		out << authServiceProxy.authenticatedUser?.username
	}

	def canDelete = { attrs, body->
		def d = attrs.remove("document")

		if (!d) {
			throwTagError("Tag [canDelete] must have [document] attribute.")
		}

		outputBody(authServiceProxy.canDelete(d), body)
	}

	def canGetSigned = { attrs, body->
		def d = attrs.remove("document")

		if (!d) {
			throwTagError("Tag [canGetSigned] must have [document] attribute.")
		}

		outputBody(authServiceProxy.canGetSigned(d), body)
	}

	def canNotes = { attrs, body->
		def d = attrs.remove("document")

		if (!d) {
			throwTagError("Tag [canNotes] must have [document] attribute.")
		}

		outputBody(authServiceProxy.canNotes(d), body)
	}

	def canNotesAny = { attrs, body->
		outputBody(authServiceProxy.canNotesAny(), body)
	}

	def canPrint = { attrs, body->
		def d = attrs.remove("document")

		if (!d) {
			throwTagError("Tag [canPrint] must have [document] attribute.")
		}

		outputBody(authServiceProxy.canPrint(d), body)
	}

	def canPrintAny = { attrs, body->
		outputBody(authServiceProxy.canPrintAny(), body)
	}

	def canSign = { attrs, body->
		def d = attrs.remove("document")

		if (!d) {
			throwTagError("Tag [canSign] must have [document] attribute.")
		}

		outputBody(authServiceProxy.canSign(d), body)
	}

	def canSignAny = { attrs, body->
		outputBody(authServiceProxy.canSignAny(), body)
	}

	def canTag = { attrs, body->
		def d = attrs.remove("document")

		if (!d) {
			throwTagError("Tag [canTag] must have [document] attribute.")
		}

		outputBody(authServiceProxy.canTag(d), body)
	}

	def canTagAny = { attrs, body->
		outputBody(authServiceProxy.canTagAny(), body)
	}

	def canUpload = { attrs, body->
		def group = attrs.remove("group")

		if (!group) {
			throwTagError("Tag [canUpload] must have [group] attribute.")
		}

		outputBody(authServiceProxy.canUpload(group), body)
	}

	def canUploadAny = { attrs, body->
		outputBody(authServiceProxy.canUploadAny(), body)
	}

	def canView = { attrs, body->
		def d = attrs.remove("document")

		if (!d) {
			throwTagError("Tag [canView] must have [document] attribute.")
		}

		outputBody(authServiceProxy.canView(d), body)
	}

	def canViewAny = { attrs, body->
		outputBody(authServiceProxy.canViewAny(), body)
	}

	def isAdmin = { attrs, body->
		outputBody(authServiceProxy.isAdmin(), body)
	}

	def facebookConnect = { attrs, body ->
		def facebook = grailsApplication.config.nimble.facebook.federationprovider.enabled

		if (attrs['secure']?.equals('true')) {
			out << render(template:"/templates/auth/facebookjs", model:[facebook:facebook, secure: true, apikey:facebookService.apiKey])
		} else {
			out << render(template:"/templates/auth/facebookjs", model:[facebook:facebook, secure: false, apikey:facebookService.apiKey])
		}
	}

	def isRunAs = { attrs, body ->
		if (authServiceProxy.authenticatedSubject.isRunAs()) {
			out << body()
		}
	}

	def canRunAs = { attrs, body ->
		def u = attrs.remove("user")

		if (!u) {
			throwTagError("Tag [canRunAs] must have [user] attribute.")
		}

		outputBody(authServiceProxy.canRunAs(u), body)
	}

	def canRunAsAny = { attrs, body ->
		outputBody(authServiceProxy.canRunAsAny(), body)
	}

	def runAsList = { attrs, body->
		out << render(template: "/auth/runas", model: [delegators:authServiceProxy.authenticatedUser?.delegators])
	}

	def delegateUsername = {
			out << authServiceProxy.delegateUser?.username
	}
}
