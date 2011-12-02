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
 * Our permission object encapsulates details that a normal Shiro deployment
 * would put into mapping tables to make life a little easier.
 *
 * @author Bradley Beddoes
 */
@MultiTenant
class Permission implements Serializable {
	static public final String defaultPerm = "org.apache.shiro.authz.permission.WildcardPermission"
	static public final String wildcardPerm = "org.apache.shiro.authz.permission.WildcardPermission"
	static public final String adminPerm = "org.apache.shiro.authz.permission.AllPermission"

	String type = defaultPerm
	String possibleActions = "*"
	String actions = "*"
	String target
	boolean managed

	User user
	Role role
	Group group

	Date dateCreated

	static belongsTo = [user: User, role: Role, group: Group]

	static transients = ["owner"]

	static mapping = {
		tenantId index: 'permission_tenant_id_idx'

		sort dateCreated: 'asc'

		cache usage: 'read-write', include: 'all'
	}

	static constraints = {
		type nullable: false, blank: false
		possibleActions nullable: false, blank: false
		actions nullable: false, blank: false
		target nullable: false, blank: false

		user nullable: true
		role nullable: true
		group nullable: true

		dateCreated nullable: true
	}

	def setOwner(def owner) {
		if (owner instanceof User) {
			this.user = owner
		}

		if (owner instanceof Role) {
			this.role = owner
		}

		if (owner instanceof Group) {
			this.group = owner
		}
	}

	def getOwner() {
		if (this.user != null) {
			return user
		}

		if (this.role != null) {
			return role
		}

		if (this.group != null) {
			return group
		}

		null
	}

	def populate(String first, String second, String third, String fourth, String fifth, String sixth) {
		first = first?.trim()

		if (!first) {
			this.errors.reject('nimble.permission.create.error')
			return
		}

		target = first
		boolean stop = false
		[second, third, fourth, fifth, sixth].each {
			if (stop) {
				return
			}

			def tmp = it?.trim()
			if (tmp) {
				target += ":$tmp"
			} else {
				stop = true
			}
		}
	}
}
