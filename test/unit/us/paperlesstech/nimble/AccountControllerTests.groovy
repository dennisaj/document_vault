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

import grails.test.*

import org.apache.shiro.crypto.hash.Sha256Hash

import us.paperlesstech.AuthService

class AccountControllerTests extends ControllerUnitTestCase {
	def asMock
	def pass
	def passConfirm
	def currentPassword

	@Override
	protected void setUp() {
		super.setUp()

		mockLogging(AccountController, true)
		asMock = mockFor(AuthService)
		controller.authService = asMock.createMock()
		pass = 'Pass123!'
		passConfirm = pass
		currentPassword = 'currentPassword'
	}

	@Override
	protected void tearDown() {
		super.tearDown()
	}

	def createValidUser() {
		def pwEnc = new Sha256Hash(currentPassword)
		def crypt = pwEnc.toHex()

		//further fill out user....

		def user = new User(id:1, username:'username', passwordHash:crypt, profile: new Profile())
		mockDomain(User, [user])

		return user
	}

	def createValidUserServiceMock() {
		def usMock = mockFor(UserService)
		return usMock
	}

	void testAllowedMethods() {
		def post = 'POST'

		assertEquals post, controller.allowedMethods.get('saveuser')
		assertEquals post, controller.allowedMethods.get('validusername')
		assertEquals post, controller.allowedMethods.get('forgottenpasswordprocess')
		assertEquals post, controller.allowedMethods.get('updatepassword')
	}

	void testChangePasswordComplete() {
		def user = new User(id:1)
		mockDomain(User, [user])
		asMock.demand.getAuthenticatedUser {-> return user}

		def model = controller.changepassword()

		assertEquals user.id, model.user.id

		asMock.verify()
	}

	void testChangePasswordNoAuth() {
		asMock.demand.getAuthenticatedUser {-> return null}

		def user = new User(id:1)
		mockDomain(User, [user])

		def model = controller.changepassword()

		assertEquals 403, mockResponse.status
		assertEquals null, model

		asMock.verify()
	}

	void testUpdatePasswordComplete() {
		def user = createValidUser()
		asMock.demand.getAuthenticatedUser {-> return user}

		def usMock = createValidUserServiceMock()
		 usMock.demand.validatePass {u, b->
			assertEquals user, u
			assertTrue b
			return true
		}

		usMock.demand.changePassword{u ->
			assertEquals user, u
			assertEquals pass, u.pass
			assertEquals passConfirm, u.passConfirm
			return user
		}
		controller.userService = usMock.createMock()
		
		mockParams.putAll( [pass:pass, passConfirm:passConfirm, currentPassword:currentPassword] )
		
		controller.updatepassword()
		
		assertEquals 200, mockResponse.status
		assertEquals 'changedpassword', controller.redirectArgs.action

		usMock.verify()
		asMock.verify()
	}

	void testUpdatePasswordNoAuth() {
		asMock.demand.getAuthenticatedUser {-> return null}

		def user = new User(id:1)
		mockDomain(User, [user])

		def model = controller.updatepassword()

		assertEquals 403, mockResponse.status
		assertEquals null, model

		asMock.verify()
	}

	void testUpdatePasswordNoCurrent() {
		def user = createValidUser()
		assertFalse user.hasErrors()

		asMock.demand.getAuthenticatedUser {-> return user}

		def pass = 'pass'
		def passConfirm = pass
		def currentPassword = 'currentPassword'

		mockLogging(AccountController, true)

		mockParams.putAll( [pass:pass, passConfirm:passConfirm] )

		def model = controller.updatepassword()

		assertEquals user.id, controller.renderArgs.model.user.id
		assertEquals 'changepassword', controller.renderArgs.view
		assertTrue controller.renderArgs.model.user.hasErrors()

		asMock.verify()
	}

	void testUpdatePasswordEmptyCurrent() {
		def user = createValidUser()
		asMock.demand.getAuthenticatedUser {-> return user}

		assertFalse user.hasErrors()

		mockParams.putAll( [pass:pass, passConfirm:passConfirm, currentPassword:''] )
		controller.updatepassword()

		assertEquals user.id, controller.renderArgs.model.user.id
		assertEquals 'changepassword', controller.renderArgs.view
		assertTrue user.hasErrors()

		asMock.verify()
	}

	void testUpdatePasswordInvalidPass() {
		def user = createValidUser()
		asMock.demand.getAuthenticatedUser {-> return user}

		def usMock = createValidUserServiceMock()
		 usMock.demand.validatePass {u, b->
			assertEquals user, u
			assertTrue b
			return true
		}

		usMock.demand.changePassword{u ->
			assertEquals user.id, u.id
			assertEquals 'notvalid', u.pass
			assertEquals passConfirm, u.passConfirm
			u.errors.rejectValue('pass', 'user.password.xyz')
			return u
		}
		controller.userService = usMock.createMock()
		
		assertFalse user.hasErrors()

		mockParams.putAll( [pass:'notvalid', passConfirm:passConfirm, currentPassword:currentPassword] )
		controller.updatepassword()

		assertEquals user.id, controller.renderArgs.model.user.id
		assertEquals 'changepassword', controller.renderArgs.view
		assertTrue user.hasErrors()

		usMock.verify()
		asMock.verify()
	}

	void testCreateUserComplete() {
		mockDomain(User, [])

		// Mock the application configuration.
		controller.grailsApplication = new Expando(config: [
			nimble: [
				localusers: [
					registration: [enabled: true]
				],
				fields: [
					enduser: [
						user: ['username'],
						profile: ['fullName']
					]
				]
			]
		])

		def model = controller.createuser()
		assertNotNull model.user
	}

	void testSaveUserComplete() {
		def user = createValidUser()
	}
}
