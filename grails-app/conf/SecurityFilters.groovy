import grails.converters.JSON
import us.paperlesstech.Document
import us.paperlesstech.Folder
import us.paperlesstech.nimble.AdminsService
import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.User

public class SecurityFilters {
	private static String openControllers = "auth|logout|account|code|userInfo|error|home"
	private static String adminControllers = "activityLog|printer|admin|admins|user|group|role"
	def dependsOn = [LoggingFilters]

	def authService
	def grailsApplication
	def notificationService

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
				accessControl(auth:false) {
					if (!authService.authenticatedUser?.enabled) {
						return false
					}

					def action = actionName ?: "index"
					log.info("user:$authService.authenticatedUser; resource:$controllerName:$action; document:$document")
					switch (controllerName) {
						case "document":
							switch (action) {
								case ["download", "image", "show"]:
									return document && (authService.canSign(document) || authService.canGetSigned(document) || authService.canView(document))
								case ["downloadImage", "thumbnail"]:
									return document && (authService.canSign(document) || authService.canGetSigned(document) || authService.canView(document))
								case ['list', 'search']:
									return true
								default:
									return false
							}
						case "party":
							switch (action) {
								case ["submitSignatures"]:
									return document && authService.canSign(document)
								case ["addParty", "submitParties", "removeParty", "resend"]:
									return document && authService.canGetSigned(document)
								default:
									return false
							}
						case 'folder':
							def folder
							def group
							if (params.folderId) {
								folder = Folder.get(params.long('folderId'))
							}

							if (params.groupId) {
								group = Group.load(params.long('groupId'))
							}

							switch (action) {
								case ['list', 'search']:
									return true
								case 'create':
									return group && authService.canManageFolders(group)
								case ['delete', 'addDocument', 'removeDocument', 'update']:
									return folder && authService.canManageFolders(folder.group)
								default:
									return false
							}
						case "note":
							return document && authService.canNotes(document)
						case "printQueue":
							return document && authService.canPrint(document)
						case "upload":
							return authService.canUploadAnyGroup()
						case "console":
							return grails.util.Environment.current == grails.util.Environment.DEVELOPMENT
						case "runAs":
							switch (action) {
								case "runas":
									def u = User.get(params.long('userId'))
									return authService.canRunAs(u)
								case "release":
									return authService.authenticatedSubject.isRunAs()
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
		filter.render([error:[statusCode:403], notification:notificationService.error('document-vault.api.securityfilters.403.error')] as JSON)
	}

	/**
	 * Copied from the Nimble security filter base
	 */
	def onNotAuthenticated(subject, filter) {
		def request = filter.request
		def response = filter.response

		if (request.xhr) {
			response.status = 401
			render([error:[statusCode:401], notification:notificationService.error('document-vault.api.securityfilters.401.error')] as JSON)
			return false
		}

		redirect(controller:'home')
	}
}
