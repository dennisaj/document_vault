package us.paperlesstech

import grails.converters.JSON
import us.paperlesstech.helpers.NotificationHelper
import us.paperlesstech.helpers.NotificationStatus

class UserInfoController {
	def authService
	def grailsApplication

	def index = {
		def n = grailsApplication.mainContext.getBean('NavigationTagLib')
		def user = authService.authenticatedUser
		if (user) {
			def menu = [:]
			n.eachItem([group:'user'], {
				menu[(g.message(code:'navigation.user.' + it.title, default:it.title, encodeAs:'HTML'))] = it.link
			})

			def delegators = [:]
			user.delegators.each {
				delegators[(it.id)] = it.profile.fullName ?: it.username
			}

			render([
				notification:NotificationHelper.success('title', 'message'),
				user:[
					id:user.id,
					delegators:delegators,
					name:user.profile?.fullName,
					username:user.username
				],
				menu:menu
			] as JSON)
		} else {
			render([notification:NotificationHelper.error('title', 'message')] as JSON)
		}
	}
}
