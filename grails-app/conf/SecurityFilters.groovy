import us.paperlesstech.Bucket
import us.paperlesstech.Document
import us.paperlesstech.Folder
import us.paperlesstech.nimble.AdminsService
import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.User

public class SecurityFilters {
	private static String openControllers = "auth|logout|account|code"
	private static String adminControllers = "activityLog|printer|admin|admins|user|group|role"
	def dependsOn = [LoggingFilters]

	def authService
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
				def group
				if (params.documentId) {
					document = Document.get(params.long('documentId'))
				}
				if (params.groupId) {
					group = Group.load(params.long('groupId'))
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
								case ["index"]:
									return authService.canViewAnyDocument() || authService.canSignAnyDocument()
								case ["sign"]:
									return document && (authService.canSign(document) || authService.canGetSigned(document))
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
						case 'bucket':
							def bucket
							if (params.bucketId) {
								bucket = Bucket.get(params.long('bucketId'))
							}

							switch (action) {
								case 'create':
									return group && authService.canCreateBucket(group)
								case 'delete':
									return bucket && authService.canDelete(bucket)
								case 'addFolder':
									return bucket && authService.canMoveInTo(bucket)
								case 'removeFolder':
									return bucket && authService.canMoveOutOf(bucket)
								default:
									return false
							}
						case 'folder':
							def folder
							if (params.folderId) {
								folder = Folder.get(params.long('bucketId'))
							}

							switch (action) {
								case 'create':
									return group && authService.canFolderCreate(group)
								case 'delete':
									return folder && authService.canFolderDelete(folder)
								case 'addDocument':
									return folder && authService.canFolderMoveInTo(folder)
								case 'removeDocument':
									return folder && authService.canFolderMoveOutOf(folder)
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
