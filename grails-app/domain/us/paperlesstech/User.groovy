package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

@MultiTenant
class User extends grails.plugins.nimble.core.UserBase {
	static SIGNATOR_USER_ROLE = "SIGNATOR_USER"
	// Extend UserBase with your custom values here

}
