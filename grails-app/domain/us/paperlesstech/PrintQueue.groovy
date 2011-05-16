package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

@MultiTenant
class PrintQueue {
	Date dateCreated
	Document document
	Printer printer
	User user

	static constraints = {
	}

	static mapping = {
	}
}
