package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

import java.util.Date

import us.paperlesstech.nimble.User

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
