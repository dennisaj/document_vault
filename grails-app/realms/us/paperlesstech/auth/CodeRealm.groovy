package us.paperlesstech.auth

import org.apache.shiro.authc.AccountException
import org.apache.shiro.authc.DisabledAccountException
import org.apache.shiro.authc.SimpleAccount
import org.apache.shiro.authc.UnknownAccountException
import us.paperlesstech.Party

class CodeRealm {
	static authTokenClass = us.paperlesstech.auth.CodeToken

	def authenticate(codeToken) {
		log.info "Attempting to authenticate $codeToken.code."
		def code = codeToken.code

		if (!code) {
			throw new AccountException("Cannot authenticate an empty code.")
		}

		def party = Party.findByCode(code)
		if (!party) {
			throw new UnknownAccountException("No party found for code [$code].")
		}

		def user = party.signator
		log.info "Located user [$user.id]$user.username in data repository, starting authentication process."

		if (!user.enabled) {
			log.warn "User [$user.id]$user.username is disabled preventing authentication."
			throw new DisabledAccountException("This account is currently disabled.")
		}

		def account = new SimpleAccount(user.id, user.passwordHash, "grails.plugins.nimble.realms.LocalizedRealm")
		log.info "Successfully logged in code [$code] as User [$user.id]$user.username."

		account
	}
}
