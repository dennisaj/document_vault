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

import javax.servlet.http.Cookie

import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.DisabledAccountException
import org.apache.shiro.authc.IncorrectCredentialsException
import org.apache.shiro.authc.UsernamePasswordToken
import org.openid4java.message.ParameterList

import us.paperlesstech.auth.nimble.FacebookConnectToken

/**
 * Manages all authentication processes including integration with OpenID, Facebook etc.
 *
 * @author Bradley Beddoes
 */
class AuthController {
	private static String TARGET = 'AuthController.TARGET'
	static Map allowedMethods = [ajaxlogin:'POST', signin:'POST']

	def authService
	def facebookService
	def grailsApplication
	def notificationService
	def openIDService
	def shiroSecurityManager
	def userService

	def beforeInterceptor = [action:this.&loggedInCheck, except:['logout', 'signout']]

	def loggedInCheck() {
		if (authService.isLoggedIn()) {
			redirect(controller:"document", action:"index")
		}
	}

	def index = {
		redirect(action:'login', params:params)
	}

	def login = {
		def local = grailsApplication.config.nimble.localusers.authentication.enabled
		def registration = grailsApplication.config.nimble.localusers.registration.enabled
		def facebook = grailsApplication.config.nimble.facebook.federationprovider.enabled
		def openid = grailsApplication.config.nimble.openid.federationprovider.enabled

		if (params.targetUri) {
			session.setAttribute(AuthController.TARGET, params.targetUri)
		}

		render(template: "/templates/nimble/login/login", model: [local: local, registration: registration, facebook: facebook, openid: openid, username: params.username, rememberMe: (params.rememberMe != null), targetUri: params.targetUri])
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
		request.xhr ? render([notification:notificationService.success('document-vault.api.logout.success')] as JSON) : redirect(uri: '/')
	}

	def unauthorized = {
		response.sendError(403)
	}

	/**
	 * OpenID integration
	 */
	def openidreq = {
		log.debug("Performing openidreq")
		performOpenIDRequest("openid", params, request, response)
	}

	def openidresp = {
		log.debug("Performing openidresp")
		processOpenIDResponse("openid", params, request, response)
	}

	def yahooreq = {
		log.debug("Performing yahooreq")
		performOpenIDRequest("yahoo", params, request, response)
	}

	def yahooresp = {
		log.debug("Performing yahooresp")
		processOpenIDResponse("yahoo", params, request, response)
	}

	def flickrreq = {
		log.debug("Performing flickrreq")
		performOpenIDRequest("flickr", params, request, response)
	}

	def flickrresp = {
		log.debug("Performing flickrresp")
		processOpenIDResponse("flickr", params, request, response)
	}

	def googlereq = {
		log.debug("Performing googlereq")
		performOpenIDRequest("google", params, request, response)
	}

	def googleresp = {
		log.debug("Performing googleresp")
		processOpenIDResponse("google", params, request, response)
	}

	def bloggerreq = {
		log.debug("Performing bloggerreq")
		performOpenIDRequest("blogger", params, request, response)
	}

	def bloggerresp = {
		log.debug("Performing bloggerresp")
		processOpenIDResponse("blogger", params, request, response)
	}

	def wordpressreq = {
		log.debug("Performing wordpressreq")
		performOpenIDRequest("wordpress", params, request, response)
	}

	def wordpressresp = {
		log.debug("Performing wordpressresp")
		processOpenIDResponse("wordpress", params, request, response)
	}

	def technoratireq = {
		if (params.technoratiusername) {
			log.debug("Performing technoratireq for $params.technoratiusername")
			params.put('openiduri', 'http://technorati.com/people/technorati/' + params.technoratiusername)
			performOpenIDRequest("technorati", params, request, response)
		} else {
			log.debug("Erronous technoratireq no username")
			flash.type = 'error'
			flash.message = message(code: "nimble.login.openid.invalid.identifier")
			redirect(action: 'login', params: [active: 'technorati'])
		}
	}

	def technoratiresp = {
		log.debug "Performing technoratiresp"
		processOpenIDResponse("technorati", params, request, response)
	}

	/**
	 * Facebook Connect integration
	 */
	def facebook = {
		if (!grailsApplication.config.nimble.facebook.federationprovider.enabled) {
			log.error "Authentication attempt for Facebook federation provider, denying attempt as Facebook disabled"
			response.sendError(403)
			return
		}

		log.info "Attempting to authenticate facebook user"

		if (request.cookies != null) {
			def currentFBSessionKey = request.cookies.find { it.name.equals(facebookService.apiKey + '_session_key') }?.value
			def currentFBSessionCookies = new Cookie[request.cookies.length]

			// Process Facebook supplied cookies per FB Connect API docs
			int i = 0
			request.cookies.each {cookie ->
				if (cookie.name.startsWith(facebookService.apiKey)) {
					currentFBSessionCookies[i] = cookie
					i++
				}
			}

			def authToken = new FacebookConnectToken(currentFBSessionKey, currentFBSessionCookies)
			try {
				authService.login(authToken)
				userService.createLoginRecord(request)

				def targetUri = session.getAttribute(AuthController.TARGET) ?: "/"
				session.removeAttribute(AuthController.TARGET)

				log.info("Authenticated facebook user. Directing to content $targetUri")
				redirect(uri: targetUri)
			} catch (AuthenticationException ex) {
				log.warn "Facebook authentication failure - ${ex.getLocalizedMessage()}"
				log.debug ex.printStackTrace()
				flash.type = 'error'
				flash.message = message(code: "nimble.login.facebook.error")
				redirect(action: 'login', params: [active: 'facebook'])
			}
		} else {
			log.warn "Facebook authentication failure - no session cookies present"
			flash.type = 'error'
			flash.message = message(code: "nimble.login.facebook.cookies")
			redirect(action: 'login', params: [active: 'facebook'])
		}
	}

	def facebookxdreciever = {}

	def facebookxdrecieverssl = {}

	private performOpenIDRequest = {service, params, request, response ->
		if (!grailsApplication.config.nimble.openid.federationprovider.enabled) {
			log.error("Authentication attempt (request) for OpenID based federation provider, denying attempt as OpenID disabled")
			response.sendError(403)
			return
		}

		log.info("Attempting to authenticate $service openID user")

		def serviceIdentifier
		def discovered
		def authRequest

		StringBuffer responseUrl = new StringBuffer(createLink(controller:"auth", action:"${service}resp", absolute: true))
		if (params.openiduri != null) {
			serviceIdentifier = params.openiduri
			(discovered, authRequest) = openIDService.establishRequest(serviceIdentifier, responseUrl.toString())
		} else {
			serviceIdentifier = service
			(discovered, authRequest) = openIDService.establishDiscoveryRequest(serviceIdentifier, responseUrl.toString())
		}

		if (discovered && authRequest) {
			log.info("Successfully discovered details for openid service $serviceIdentifier redirecting client")

			session.setAttribute("discovered", discovered)
			if (!discovered.isVersion2()) {
				response.sendRedirect(authRequest.getDestinationUrl(true))
				return
			}

			render(view: "openidreq", model: [openidreqparams: authRequest.getParameterMap(), destination: authRequest.getDestinationUrl(false)])
		} else {
			log.warn("Unable to discover details for openid service $serviceIdentifier redirecting client")

			flash.type = 'error'
			flash.message = message(code: "nimble.login.openid.${service}.internal.error.req")
			redirect(action: 'login', params: [active: service])
		}
	}

	private processOpenIDResponse = {service, params, request, response ->

		if (!grailsApplication.config.nimble.openid.federationprovider.enabled) {
			log.error("Authentication attempt (response) for OpenID based federation provider, denying attempt as OpenID disabled")
			response.sendError(403)
			return
		}

		def discovered = session.getAttribute("discovered")
		ParameterList openIDResponse = new ParameterList(request.getParameterMap())

		StringBuffer receivingUrl = new StringBuffer(createLink(action: "${service}resp", absolute: true))
		String queryString = request.getQueryString()
		if (queryString != null && queryString.length() > 0)
		receivingUrl.append("?").append(request.getQueryString())

		def authToken = openIDService.processResponse(discovered, openIDResponse, receivingUrl.toString())

		if (authToken) {
			try {
				authService.login(authToken)
				userService.createLoginRecord(request)

				def targetUri = session.getAttribute(AuthController.TARGET) ?: "/"
				session.removeAttribute(AuthController.TARGET)

				log.info("Authenticated openID user $authToken.userID. Directing to content $targetUri")
				redirect(uri: targetUri)
			} catch (AuthenticationException ex) {
				log.warn "OpenID authentication failure for $authToken.userID - ${ex.getLocalizedMessage()}"
				log.debug ex.printStackTrace()
				flash.type = 'error'
				flash.message = message(code: "nimble.login.openid.${service}.failed")
				redirect(action: 'login', params: [active: service])
			}
		} else {
			log.debug "OpenID authentication failure"

			flash.type = 'error'
			flash.message = message(code: "nimble.login.openid.${service}.internal.error.res")
			redirect(action: 'login', params: [active: service])
		}
	}
}
