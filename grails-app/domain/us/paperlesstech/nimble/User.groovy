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

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

import org.codehaus.groovy.grails.commons.ConfigurationHolder

import us.paperlesstech.Preference

/**
 * Represents a user within a Nimble Application
 *
 * @author Bradley Beddoes
 */
@MultiTenant
class User implements Comparable<User> {
	static final String SIGNATOR_USER_ROLE = "SIGNATOR_USER"

	String username
	String realm
	String passwordHash
	String actionHash
	boolean enabled
	boolean external
	boolean federated
	boolean remoteapi = false

	FederationProvider federationProvider
	Profile profile

	Date expiration
	Date dateCreated
	Date lastUpdated

	String externalId
	SortedSet delegators

	static belongsTo = [Role, Group]

	static hasMany = [
		passwdHistory: String,
		loginRecords: LoginRecord,
		follows: User,
		followers: User,
		roles: Role,
		groups: Group,
		permissions: Permission,
		preferences:Preference,
		delegators: User
	]

	static mapping = {
		sort username:'desc'

		cache usage: 'read-write', include: 'all'
		table ConfigurationHolder.config?.nimble?.tablenames?.user

		profile lazy: false

		roles cache: true, cascade: 'none'
		groups cache: true, cascade: 'none'
		permissions cache: true
		externalId index: "user_external_id_idx"
		preferences cache:true
		delegators nullable:true, joinTable:[name:'users_delegators']
	}

	static constraints = {
		username blank: false, unique: 'tenantId', minSize: 4, maxSize: 255
		passwordHash nullable: true, blank: false
		actionHash nullable: true, blank: false
		realm nullable: true, blank: false

		federationProvider nullable: true
		profile nullable:false

		expiration nullable: true

		dateCreated nullable: true
		lastUpdated nullable: true

		permissions nullable:true
		externalId nullable: true, blank: true
		preferences nullable:true
	}

	@Override
	String toString() {
		"User($id) $username"
	}

	/**
	 * Sort by profile.fullName by fall back to username if profile.fullName is empty
	 */
	@Override
	int compareTo(User other) {
		(profile?.fullName ?: username).toLowerCase() <=> (other?.profile?.fullName ?: other?.username).toLowerCase()
	}

	// Transients
	static transients = ['pass', 'passConfirm']
	String pass
	String passConfirm
}
