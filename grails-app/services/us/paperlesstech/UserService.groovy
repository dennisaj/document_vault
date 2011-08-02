package us.paperlesstech

import grails.plugins.nimble.InstanceGenerator
import grails.plugins.nimble.core.Role
import us.paperlesstech.helpers.UserHelpers

class UserService extends grails.plugins.nimble.core.UserService {
	def roleService

	User createUser(Map map) {
		String username = map.username
		assert username
		String fullName = map.fullName
		assert fullName
		String email = map.email
		String externalId = map.externalId
		String realm = map.realm
		boolean addSignatorRole = map.addSignatorRole ?: false

		def password = UserHelpers.generatePassword(grailsApplication.config.nimble.passwords.minlength)
		def user = InstanceGenerator.user()
		user.profile = InstanceGenerator.profile()

		user.username = username
		user.enabled = true
		user.external = false
		user.pass = password
		user.passConfirm = password
		user.realm = realm
		user.externalId = externalId

		user.profile.fullName = fullName
		user.profile.email = email
		user.profile.owner = user

		def savedUser = createUser(user)
		if (savedUser.hasErrors()) {
			return user
		}

		if (addSignatorRole) {
			roleService.addMember(savedUser, Role.findByName(User.SIGNATOR_USER_ROLE))
		}

		savedUser
	}
}
