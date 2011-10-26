package us.paperlesstech

import grails.converters.JSON
import us.paperlesstech.helpers.NotificationHelper

class UserInfoController {
	def authService
	def grailsApplication

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
				returnMap.user.delegateUser = [
					id:authService.delegateUser?.id,
					name:authService.delegateUser?.profile?.fullName,
					username:authService.delegateUser?.username
				]
			}

			returnMap.user.groups = [:]

			returnMap.user.groups.upload = authService.getGroupsWithPermission([DocumentPermission.Upload])*.asMap()
			returnMap.user.groups.manageFolders = authService.getGroupsWithPermission([DocumentPermission.ManageFolders])*.asMap()

			returnMap.notification = NotificationHelper.success('title', 'message')

			render(returnMap as JSON)
		} else {
			render([notification:NotificationHelper.error('title', 'message')] as JSON)
		}
	}
}
