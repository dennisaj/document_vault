package us.paperlesstech

import org.codehaus.groovy.grails.web.pages.GroovyPage

import us.paperlesstech.nimble.User

class AuthTagLib {
	static final namespace = "pt"

	def authService
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
		if (authService.isLoggedIn()) {
			out << body()
		}
	}

	def username = { attrs, body ->
		out << authService.authenticatedUser?.username
	}

	def canDelete = { attrs, body->
		def d = attrs.remove("document")

		if (!d) {
			throwTagError("Tag [canDelete] must have [document] attribute.")
		}

		outputBody(authService.canDelete(d), body)
	}

	def canGetSigned = { attrs, body->
		def d = attrs.remove("document")

		if (!d) {
			throwTagError("Tag [canGetSigned] must have [document] attribute.")
		}

		outputBody(authService.canGetSigned(d), body)
	}

	def canNotes = { attrs, body->
		def d = attrs.remove("document")

		if (!d) {
			throwTagError("Tag [canNotes] must have [document] attribute.")
		}

		outputBody(authService.canNotes(d), body)
	}

	def canNotesAnyDocument = { attrs, body->
		outputBody(authService.canNotesAnyDocument(), body)
	}

	def canPrint = { attrs, body->
		def d = attrs.remove("document")

		if (!d) {
			throwTagError("Tag [canPrint] must have [document] attribute.")
		}

		outputBody(authService.canPrint(d), body)
	}

	def canPrintAnyDocument = { attrs, body->
		outputBody(authService.canPrintAnyDocument(), body)
	}

	def canSign = { attrs, body->
		def d = attrs.remove("document")

		if (!d) {
			throwTagError("Tag [canSign] must have [document] attribute.")
		}

		outputBody(authService.canSign(d), body)
	}

	def canSignAnyDocument = { attrs, body->
		outputBody(authService.canSignAnyDocument(), body)
	}

	def canUpload = { attrs, body->
		def group = attrs.remove("group")

		if (!group) {
			throwTagError("Tag [canUpload] must have [group] attribute.")
		}

		outputBody(authService.canUpload(group), body)
	}

	def canUploadAnyGroup = { attrs, body->
		outputBody(authService.canUploadAnyGroup(), body)
	}

	def canView = { attrs, body->
		def d = attrs.remove("document")

		if (!d) {
			throwTagError("Tag [canView] must have [document] attribute.")
		}

		outputBody(authService.canView(d), body)
	}

	def canViewAnyDocument = { attrs, body->
		outputBody(authService.canViewAnyDocument(), body)
	}

	def isAdmin = { attrs, body->
		outputBody(authService.isAdmin(), body)
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
		if (authService.authenticatedSubject.isRunAs()) {
			out << body()
		}
	}

	def canRunAs = { attrs, body ->
		def u = attrs.remove("user")

		if (!u) {
			throwTagError("Tag [canRunAs] must have [user] attribute.")
		}

		outputBody(authService.canRunAs(u), body)
	}

	def canRunAsAny = { attrs, body ->
		outputBody(authService.canRunAsAny(), body)
	}

	def runAsList = { attrs, body->
		out << render(template: "/auth/runas", model: [delegators:authService.authenticatedUser?.delegators])
	}

	def delegateUsername = {
			out << authService.delegateUser?.username
	}
}
