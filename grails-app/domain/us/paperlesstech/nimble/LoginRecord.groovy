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

/**
 * Represents a system that a user logged into a Nimble based application from
 *
 * @author Bradley Beddoes
 */
@MultiTenant
class LoginRecord {
	String remoteAddr
	String remoteHost
	String userAgent

	Date dateCreated
	Date lastUpdated

	static belongsTo = [owner: User]

	static mapping = {
		tenantId index: 'login_record_tenant_id_idx'
	}

	static constraints = {
		remoteAddr nullable: false, blank: false
		remoteHost nullable: false, blank: false
		userAgent nullable: false, blank: false

		dateCreated nullable: true
		lastUpdated nullable: true
	}
}
