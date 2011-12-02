package us.paperlesstech

import grails.converters.JSON

class UserInfoController {
	def authService
	def grailsApplication
	def notificationService
	def tenantService

	def index = {
		def n = grailsApplication.mainContext.getBean('NavigationTagLib')
		def user = authService.authenticatedUser
		if (user) {
			def returnMap = [:]
			returnMap.menu = []
			n.eachItem([group:'user']) {
				returnMap.menu << [
					title:g.message(code:'navigation.user.' + it.title, default:it.title, encodeAs:'HTML'),
					url:it.link
				]
			}

			returnMap.user = [
				id:user.id,
				name:user.profile?.fullName,
				username:user.username
			]

			if (user.delegators) {
				returnMap.user.delegators = []
				user.delegators.each {
					returnMap.user.delegators << [
						id:it.id,
						name:it.profile.fullName ?: it.username
					]
				}
			}

			def delegateUser = authService.delegateUser
			if (delegateUser) {
				returnMap.user.delegators = []
				returnMap.user.delegateUser = [
					id:delegateUser?.id,
					name:delegateUser?.profile?.fullName,
					username:delegateUser?.username
				]
			}

			returnMap.flags = tenantService.getTenantConfigList('flag')

			returnMap.user.groups = [:]

			returnMap.user.groups.upload = authService.getGroupsWithPermission([DocumentPermission.Upload])*.asMap()
			returnMap.user.groups.manageFolders = authService.getGroupsWithPermission([DocumentPermission.ManageFolders])*.asMap()

			returnMap.user.pinnedFolders = user.pinnedFolders*.asMap()

			returnMap.notification = notificationService.success('document-vault.api.userinfo.userfound')

			render(returnMap as JSON)
		} else {
			render([notification:notificationService.error('document-vault.api.userinfo.usernotfound')] as JSON)
		}
	}
}
