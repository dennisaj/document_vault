/*
 *  Nimble, an extensive application base for Grails
 *  Copyright (C) 2010 Bradley Beddoes
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
import grails.plugins.nimble.core.AdminsService
import us.paperlesstech.Document

/**
 * Filter that works with Nimble security model to protect controllers, actions, views
 *
 * @author Bradley Beddoes
 */
public class NimbleSecurityFilters extends grails.plugins.nimble.security.NimbleFilterBase {
	private static String openControllers = "auth|logout|account|code"
	private static String adminControllers = "activityLog|printer|admin|admins|user|group|role"
	def dependsOn = [LoggingFilters]
	def authService

	def filters = {
		secure(controller: "($openControllers|$adminControllers)", invert: true) {
			before = {
				def document
				if (params.documentId) {
					document = Document.get(params.documentId)
				}
				accessControl (auth: false) {
					def action = actionName ?: "index"
					log.info("user:$authService.authenticatedUser; resource:$controllerName:$action; document:$document")
					switch (controllerName) {
						case ["document", "home"]:
							switch (action) {
								case ["download", "downloadImage", "image", "show"]:
									return document && (authService.canSign(document) || authService.canGetSigned(document) || authService.canView(document))
								case ["index"]:
									return authService.canViewAny() || authService.canSignAny()
								case ["sign"]:
									return document && (authService.canSign(document) || authService.canGetSigned(document))
								case ["submitSignatures"]:
									return document && (authService.canSign(document))
								case ["note", "saveNote"]:
									return document && (authService.canNotes(document))
								case ["addParty", "submitParties", "removeParty"]:
									return document && (authService.canGetSigned(document))
								case "resend":
									return document && (authService.canGetSigned(document))
								default:
									return false
							}
						case "printQueue":
							return document ? authService.canPrint(document) : authService.canPrintAny()
						case "tag":
							return document ? authService.canTag(document) : authService.canTagAny()
						case "upload":
							return authService.canUploadAny()
						case "console":
							return grails.util.Environment.current == grails.util.Environment.DEVELOPMENT
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
}
