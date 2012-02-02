import grails.converters.JSON

import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

import us.paperlesstech.Document
import us.paperlesstech.DocumentPermission
import us.paperlesstech.Folder
import us.paperlesstech.nimble.AdminsService
import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.User

public class SecurityFilters implements ApplicationContextAware {
	private static String openControllers = "auth|logout|account|code|userInfo|error"
	private static String adminControllers = "activityLog|printer|admin|admins|user|group|role|configuration"
	def dependsOn = [LoggingFilters]

	private ApplicationTagLib g
	def authService
	def grailsApplication
	def notificationService
	def requestService

	def filters = {
		secure(controller: "($openControllers|$adminControllers)", invert: true) {
			before = {
				// Create a special case for party/codeSignatures so we can expose it to users with one-off permissions.
				// Permissions checking will be done once in the action itself.
				if (controllerName == 'party' && actionName in ['codeSign', 'codeClickWrap']) {
					return true
				}

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
								case ['flag', 'unflag']:
									return document && authService.canFlag(document)
								default:
									return false
							}
						case "party":
							switch (action) {
								case ["cursiveSign"]:
									return document && authService.canSign(document)
								case ["emailDocument", "submitParties", "remove", "resend"]:
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
								case ['pin', 'show']:
									return folder && folder.group in authService.getGroupsWithPermission([DocumentPermission.ManageFolders, DocumentPermission.GetSigned, DocumentPermission.Sign, DocumentPermission.View])
								case ['list', 'search', 'unpin']:
									return true
								case 'create':
									return group && authService.canManageFolders(group)
								case ['delete', 'addDocument', 'removeDocument', 'update', 'addFolder', 'removeFolder', 'flag', 'unflag']:
									return folder && authService.canManageFolders(folder.group)
								default:
									return false
							}
						case "note":
							switch (action) {
								case ['download', 'saveLines']:
									// Disable handwritten notes for now.
									return false
								default:
									return document && authService.canNotes(document)
							}
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
									return u && authService.canRunAs(u)
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
		def request = filter.request
		def response = filter.response

		if (request.xhr) {
			response.status = 403
			filter.render([error: [statusCode: 403], notification: notificationService.error('document-vault.api.securityfilters.403.error')] as JSON)
			return false
		}

		def url = g.createLink(mapping: 'homePage', base: requestService.baseAddr)
		filter.redirect url: url
	}

	/**
	 * Copied from the Nimble security filter base
	 */
	def onNotAuthenticated(subject, filter) {
		def request = filter.request
		def response = filter.response

		if (request.xhr) {
			response.status = 401
			filter.render([error:[statusCode:401], notification:notificationService.error('document-vault.api.securityfilters.401.error')] as JSON)
			return false
		}

		def url = g.createLink(mapping: 'homePage', base: requestService.baseAddr)
		filter.redirect url: url
	}

	void setApplicationContext(ApplicationContext applicationContext) {
		g = applicationContext.getBean(ApplicationTagLib)
	}
}
