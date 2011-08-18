/*
 * Nimble, an extensive application base for Grails
 * Copyright (C) 2010 Bradley Beddoes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package us.paperlesstech.auth.nimble

import org.apache.shiro.authc.AuthenticationToken
import org.openid4java.discovery.Identifier

/**
 * Token used with OpenID Shiro realm for authentication purposes
 *
 * @author Bradley Beddoes
 */
public class OpenIDToken implements AuthenticationToken {
	def email
	def fullName
	def gender
	def nickName
	Identifier principal

	public OpenIDToken(Identifier identifier) {
		this.principal = identifier
	}

	/**
	 * Returns the users validated OpenID identifier
	 */
	@Override
	public Object getPrincipal() {
		return this.principal
	}

	/**
	 * Returns null for OpenID
	 */
	@Override
	public Object getCredentials() {
		return null
	}

	public String getUserID() {
		return this.principal.getIdentifier()
	}

	public Identifier getOpenIDIdentifier() {
		return this.principal
	}
}
