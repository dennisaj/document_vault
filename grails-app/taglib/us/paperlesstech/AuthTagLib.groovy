package us.paperlesstech

import org.codehaus.groovy.grails.web.pages.GroovyPage

class AuthTagLib {
	static namespace = "pt"

	def authService

	def isLoggedIn = {attrs, body ->
		if (authService.isLoggedIn()) {
			out << body()
		}
	}

	def username = {attrs, body ->
		out << authService.authenticatedUser?.username
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

		outputBody(authService.canDelete(d), body)
	}

	def canGetSigned = {attr, body->
		def d = attr.remove("document")

		if (!d) {
			throwTagError("Tag [canGetSigned] must have [document] attribute.")
		}

		outputBody(authService.canGetSigned(d), body)
	}

	def canNotes = {attr, body->
		def d = attr.remove("document")

		if (!d) {
			throwTagError("Tag [canNotes] must have [document] attribute.")
		}

		outputBody(authService.canNotes(d), body)
	}

	def canNotesAny = {attr, body->
		outputBody(authService.canNotesAny(), body)
	}

	def canPrint = {attr, body->
		def d = attr.remove("document")

		if (!d) {
			throwTagError("Tag [canPrint] must have [document] attribute.")
		}

		outputBody(authService.canPrint(d), body)
	}

	def canPrintAny = {attr, body->
		outputBody(authService.canPrintAny(), body)
	}

	def canSign = {attr, body->
		def d = attr.remove("document")

		if (!d) {
			throwTagError("Tag [canSign] must have [document] attribute.")
		}

		outputBody(authService.canSign(d), body)
	}

	def canSignAny = {attr, body->
		outputBody(authService.canSignAny(), body)
	}

	def canTag = {attr, body->
		def d = attr.remove("document")

		if (!d) {
			throwTagError("Tag [canTag] must have [document] attribute.")
		}

		outputBody(authService.canTag(d), body)
	}

	def canTagAny = {attr, body->
		outputBody(authService.canTagAny(), body)
	}

	def canUpload = {attr, body->
		def group = attr.remove("group")

		if (!group) {
			throwTagError("Tag [canUpload] must have [group] attribute.")
		}

		outputBody(authService.canUpload(group), body)
	}

	def canUploadAny = {attr, body->
		outputBody(authService.canUploadAny(), body)
	}

	def canView = {attr, body->
		def d = attr.remove("document")

		if (!d) {
			throwTagError("Tag [canView] must have [document] attribute.")
		}

		outputBody(authService.canView(d), body)
	}

	def canViewAny = {attr, body->
		outputBody(authService.canViewAny(), body)
	}
}
