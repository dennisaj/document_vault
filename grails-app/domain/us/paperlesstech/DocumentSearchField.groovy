package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

@MultiTenant
class DocumentSearchField extends DocumentField {
	static mapping = {
		key column:"_key"
		value column:"_value"
	}
}
