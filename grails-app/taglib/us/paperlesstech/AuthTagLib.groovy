package us.paperlesstech

import org.codehaus.groovy.grails.web.pages.GroovyPage

class AuthTagLib {
	static namespace = "pt"

	def authServiceProxy
	def facebookService

	def isLoggedIn = {attrs, body ->
		if (authServiceProxy.isLoggedIn()) {
			out << body()
		}
	}

	def username = {attrs, body ->
		out << authServiceProxy.authenticatedUser?.username
	}

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

	def canDelete = {attr, body->
		def d = attr.remove("document")

		if (!d) {
			throwTagError("Tag [canDelete] must have [document] attribute.")
		}

		outputBody(authServiceProxy.canDelete(d), body)
	}

	def canGetSigned = {attr, body->
		def d = attr.remove("document")

		if (!d) {
			throwTagError("Tag [canGetSigned] must have [document] attribute.")
		}

		outputBody(authServiceProxy.canGetSigned(d), body)
	}

	def canNotes = {attr, body->
		def d = attr.remove("document")

		if (!d) {
			throwTagError("Tag [canNotes] must have [document] attribute.")
		}

		outputBody(authServiceProxy.canNotes(d), body)
	}

	def canNotesAny = {attr, body->
		outputBody(authServiceProxy.canNotesAny(), body)
	}

	def canPrint = {attr, body->
		def d = attr.remove("document")

		if (!d) {
			throwTagError("Tag [canPrint] must have [document] attribute.")
		}

		outputBody(authServiceProxy.canPrint(d), body)
	}

	def canPrintAny = {attr, body->
		outputBody(authServiceProxy.canPrintAny(), body)
	}

	def canSign = {attr, body->
		def d = attr.remove("document")

		if (!d) {
			throwTagError("Tag [canSign] must have [document] attribute.")
		}

		outputBody(authServiceProxy.canSign(d), body)
	}

	def canSignAny = {attr, body->
		outputBody(authServiceProxy.canSignAny(), body)
	}

	def canTag = {attr, body->
		def d = attr.remove("document")

		if (!d) {
			throwTagError("Tag [canTag] must have [document] attribute.")
		}

		outputBody(authServiceProxy.canTag(d), body)
	}

	def canTagAny = {attr, body->
		outputBody(authServiceProxy.canTagAny(), body)
	}

	def canUpload = {attr, body->
		def group = attr.remove("group")

		if (!group) {
			throwTagError("Tag [canUpload] must have [group] attribute.")
		}

		outputBody(authServiceProxy.canUpload(group), body)
	}

	def canUploadAny = {attr, body->
		outputBody(authServiceProxy.canUploadAny(), body)
	}

	def canView = {attr, body->
		def d = attr.remove("document")

		if (!d) {
			throwTagError("Tag [canView] must have [document] attribute.")
		}

		outputBody(authServiceProxy.canView(d), body)
	}

	def canViewAny = {attr, body->
		outputBody(authServiceProxy.canViewAny(), body)
	}

	def isAdmin = { attr, body->
		outputBody(authServiceProxy.isAdmin(), body)
	}

	def facebookConnect = {attrs, body ->
		def facebook = grailsApplication.config.nimble.facebook.federationprovider.enabled

		if (attrs['secure']?.equals('true'))
		out << render(template: "/templates/auth/facebookjs", contextPath: pluginContextPath, model: [facebook:facebook, secure: true, apikey: facebookService.apiKey])
		else
		out << render(template: "/templates/auth/facebookjs", contextPath: pluginContextPath, model: [facebook:facebook, secure: false, apikey: facebookService.apiKey])
	}
}
