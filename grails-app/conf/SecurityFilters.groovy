import us.paperlesstech.Document
import us.paperlesstech.nimble.AdminsService
import us.paperlesstech.nimble.User

public class SecurityFilters {
	private static String openControllers = "auth|logout|account|code"
	private static String adminControllers = "activityLog|printer|admin|admins|user|group|role"
	def dependsOn = [LoggingFilters]

	def authServiceProxy
	def grailsApplication

	def filters = {
		remoteSigning(controller: "code") {
			before = {
				if (!grailsApplication.config.document_vault.remoteSigning.enabled) {
					response.sendError(403)
				}
			}
		}

		secure(controller: "($openControllers|$adminControllers)", invert: true) {
			before = {
				def document
				if (params.documentId) {
					document = Document.get(params.long('documentId'))
				}
				accessControl (auth: false) {
					def action = actionName ?: "index"
					log.info("user:$authServiceProxy.authenticatedUser; resource:$controllerName:$action; document:$document")
					switch (controllerName) {
						case "document":
							switch (action) {
								case ["download", "image", "show"]:
									return document && (authServiceProxy.canSign(document) || authServiceProxy.canGetSigned(document) || authServiceProxy.canView(document))
								case ["downloadImage", "thumbnail"]:
									return document && (authServiceProxy.canTag(document) || authServiceProxy.canSign(document) || authServiceProxy.canGetSigned(document) || authServiceProxy.canView(document))
								case ["index"]:
									return authServiceProxy.canViewAny() || authServiceProxy.canSignAny()
								case ["sign"]:
									return document && (authServiceProxy.canSign(document) || authServiceProxy.canGetSigned(document))
								case ["submitSignatures"]:
									return document && (authServiceProxy.canSign(document))
								case ["addParty", "submitParties", "removeParty"]:
									return document && (authServiceProxy.canGetSigned(document))
								case "resend":
									return document && (authServiceProxy.canGetSigned(document))
								default:
									return false
							}
						case "note":
							return document && (authServiceProxy.canNotes(document))
						case "printQueue":
							return document ? authServiceProxy.canPrint(document) : authServiceProxy.canPrintAny()
						case "tag":
							return document ? authServiceProxy.canTag(document) : authServiceProxy.canTagAny()
						case "upload":
							return authServiceProxy.canUploadAny()
						case "console":
							return grails.util.Environment.current == grails.util.Environment.DEVELOPMENT
						case "runAs":
							switch (action) {
								case "runas":
									def u = User.get(params.long('userId'))
									return authServiceProxy.canRunAs(u)
								case "release":
									return authServiceProxy.authenticatedSubject.isRunAs()
								default:
									return false
							}
						default:
							return false
					}

					return false
				}
			}
		}

		// This should be extended as the application adds more administrative functionality
		administration(controller: "($adminControllers)") {
			before = {
				accessControl {
					role(AdminsService.ADMIN_ROLE)
				}
			}
		}
	}

	def onUnauthorized(subject, filter) {
		filter.response.status = 403
		filter.render view:"/unauthorized"
	}

	/**
	 * Copied from the Nimble security filter base
	 */
	def onNotAuthenticated(subject, filter) {
		def request = filter.request
		def response = filter.response

		// If this is an ajax request we want to send a 403 so the UI can act accordingly (generally log the user in again)
		if (request.getHeader('X-REQUESTED-WITH')) {
			response.status = 403
			response.setHeader("X-Nim-Session-Invalid", "true")
			return false
		}

		// Default behaviour is to redirect to the login page.
		def targetUri = request.forwardURI - request.contextPath
		def query = request.queryString
		if (query) {
			if (!query.startsWith('?')) {
				query = '?' + query
			}
			targetUri += query
		}

		filter.redirect(controller: 'auth', action: 'login', params: [targetUri: targetUri])
	}
}
