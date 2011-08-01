package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

@MultiTenant
class User extends grails.plugins.nimble.core.UserBase {
	static SIGNATOR_USER_ROLE = "SIGNATOR_USER"

	String externalId

	static constraints = {
		externalId nullable: true, blank: true
	}

	static mapping = {
		externalId index: "user_external_id_idx"
	}
}
