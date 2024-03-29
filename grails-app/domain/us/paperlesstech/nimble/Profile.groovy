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

import grails.plugin.multitenant.core.annotation.MultiTenant

import org.apache.shiro.crypto.hash.Md5Hash

/**
 * Represents generic details about users that are useful to many applications
 *
 * @author Bradley Beddoes
 */
@MultiTenant
class Profile {
	String fullName
	String nickName
	String email
	String nonVerifiedEmail
	String emailHash

	Date dateCreated
	Date lastUpdated

	def beforeInsert = {
		hashEmail()
	}

	def beforeUpdate = {
		hashEmail()
	}

	def hashEmail = {
		// Do MD5 hash of email for Gravatar
		if (email) {
			def hasher = new Md5Hash(email)
			emailHash = hasher.toHex()
		}
	}

	static belongsTo = [owner:User]

	static mapping = {
		tenantId index: 'profile_tenant_id_idx'

		cache usage: 'read-write', include: 'all'
	}

	static constraints = {
		fullName nullable: true, blank: false
		nickName nullable: true, blank: false
		email nullable:true, blank:false, email: true, unique: "tenantId"
		nonVerifiedEmail nullable:true, blank:false, email: true
		emailHash nullable: true, blank:true
		dateCreated nullable: true
		lastUpdated nullable: true
	}
}
