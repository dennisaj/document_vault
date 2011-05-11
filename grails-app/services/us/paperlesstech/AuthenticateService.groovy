package us.paperlesstech

import org.apache.shiro.SecurityUtils

class AuthenticateService {
	static transactional = false

	User userDomain() {
		def username = SecurityUtils.subject.principal

		def user = User.findByUsername(username)
		assert user, "User is required here"

		user
	}
}
