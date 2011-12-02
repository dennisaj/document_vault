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
 * Represents a grouping of users in a Nimble based application
 *
 * @author Bradley Beddoes
 */
@MultiTenant
class Group implements Comparable<Group> {
	String name
	String description
	String realm

	String externalId

	boolean external = false
	boolean protect = false

	Date dateCreated
	Date lastUpdated

	static hasMany = [
		roles: Role,
		users: User,
		permissions: Permission
	]

	static mapping = {
		tenantId index: 'group_tenant_id_idx'

		cache usage: 'read-write', include: 'all'
		table '_group'

		users cache: true, joinTable: [name: '_group_to_user']
		roles cache: true, joinTable: [name: '_group_to_role']
		permissions cache: true

		description index: 'group_description_idx'
		externalId index: 'group_external_id_idx'
		external column: '_external'
	}

	static constraints = {
		name blank: false, unique: 'tenantId', minSize:1, maxSize: 255
		description nullable: true, blank: false
		realm nullable: true, blank: false
		dateCreated nullable: true
		lastUpdated nullable: true
		permissions nullable:true
		externalId nullable: true, blank: true
	}

	@Override
	int compareTo(Group other) {
		name <=> other?.name
	}

	@Override
	String toString() {
		"Group($id) - $name"
	}

	Map asMap() {
		[
			id:id,
			name:name,
			description:description
		]
	}
}
