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

/**
 * Represents a role within a Nimble application
 *
 * @author Bradley Beddoes
 */
@MultiTenant
class Role {
	String name
	String description
	String realm
	
	boolean external = false
	boolean protect = false

	Date dateCreated
	Date lastUpdated

	static hasMany = [
		users: User,
		groups: Group,
		permissions: Permission
	]

	static belongsTo = [Group]

	static mapping = {
		cache usage: 'read-write', include: 'all'
		table '_role'

		external column: '_external'
		users cache: true, joinTable: [name: '_role_to_user']
		groups cache: true, joinTable: [name: '_group_to_role']
		permissions cache: true
	}

	static constraints = {
		name blank: false, minSize:4, maxSize: 255, unique: 'tenantId'
		description nullable:true, blank:false
		realm nullable:true, blank:false

		dateCreated nullable: true
		lastUpdated nullable: true

		permissions nullable:true
	}
}
