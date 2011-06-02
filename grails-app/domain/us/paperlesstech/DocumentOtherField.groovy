package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

@MultiTenant
class DocumentOtherField extends DocumentField {
	String fieldType = "DocumentOtherField"

	static mapping = {
		discriminator "1"
	}
}
