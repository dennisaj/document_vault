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
package us.paperlesstech.nimble

import grails.converters.JSON

import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.DisabledAccountException
import org.apache.shiro.authc.IncorrectCredentialsException
import org.apache.shiro.authc.UsernamePasswordToken

/**
 * Manages all authentication processes
 *
 * @author Bradley Beddoes
 */
class AuthController {
	private static String TARGET = 'AuthController.TARGET'
	static Map allowedMethods = [ajaxlogin:'POST', signin:'POST']

	def authService
	def grailsApplication
	def notificationService
	def requestService
	def shiroSecurityManager
	def userService

	def beforeInterceptor = [action:this.&loggedInCheck, except:['logout', 'signout']]

	def loggedInCheck() {
		if (authService.isLoggedIn()) {
			def url = g.createLink(mapping: 'homePage', base: requestService.baseAddr)
			redirect url: url
		}
	}

	def signin = {
		def authToken = new UsernamePasswordToken(params.username, params.password)

		if (params.rememberme) {
			authToken.rememberMe = true
		}

		log.info("Attempting to authenticate user, $params.username. RememberMe is $authToken.rememberMe")

		try {
			
			authService.login(authToken)
			userService.createLoginRecord(request)

			def targetUri = session.getAttribute(AuthController.TARGET) ?: "/"
			session.removeAttribute(AuthController.TARGET)

			log.info "Authenticated user, $params.username."
			log.info "Directing to content $targetUri"
			redirect(uri:targetUri)
			return
		} catch (IncorrectCredentialsException e) {
			log.info "Credentials failure for user '${params.username}'."
			log.debug e
			flash.type = 'error'
			flash.message = g.message(code: "nimble.login.failed.credentials")
		} catch (DisabledAccountException e) {
			log.info "Attempt to login to disabled account for user '${params.username}'."
			log.debug e

			flash.type = 'error'
			flash.message = g.message(code: "nimble.login.failed.disabled")
		} catch (AuthenticationException e) {
			log.info "General authentication failure for user '${params.username}'."
			log.debug e

			flash.type = 'error'
			flash.message = g.message(code: "nimble.login.failed.general")
		}

		params.remove('password')
		login()
	}

	def logout = {
		signout()
	}

	def ajaxlogin = {
		def authToken = new UsernamePasswordToken(params.username, params.password)
		def notification

		if (params.rememberme) {
			authToken.rememberMe = true
		}

		log.info("Attempting to authenticate user, $params.username. RememberMe is $authToken.rememberMe")

		try {
			authService.login(authToken)
			userService.createLoginRecord(request)

			log.info "Authenticated user, $params.username."

			render([notification:notificationService.success('document-vault.api.login.success')] as JSON)
			return
		} catch (IncorrectCredentialsException e) {
			log.info "Credentials failure for user '${params.username}'."
			log.debug e
				
			notification = 'nimble.login.failed.credentials'
		} catch (DisabledAccountException e) {
			log.info "Attempt to login to disabled account for user '${params.username}'."
			log.debug e

			notification = 'nimble.login.failed.disabled'
		} catch (AuthenticationException e) {
			log.info "General authentication failure for user '${params.username}'."
			log.debug e

			notification = 'nimble.login.failed.general'
		}

		render([notification:notificationService.error(notification)] as JSON)
	}

	def signout = {
		log.info("Signing out user ${authService.authenticatedUser?.username}")

		if (authService.authenticatedSubject?.isRunAs()) {
			authService.authenticatedSubject.releaseRunAs()
		} else {
			authService.logout()
		}

		if (request.xhr) {
			render([notification: notificationService.success('document-vault.api.logout.success')] as JSON)
		} else {
			def url = g.createLink(mapping: 'homePage', base: requestService.baseAddr)
			redirect url: url
		}
	}

	def unauthorized = {
		response.sendError(403)
	}
}
