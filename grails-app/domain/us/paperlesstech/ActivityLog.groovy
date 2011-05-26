package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

@MultiTenant
class ActivityLog {
	Date dateCreated
	String ip
	String params
	User user
	String userAgent
	String uri

	static mapping = {
	}

	static constraints = {
		params nullable:true, blank:true, maxSize:4096
		user nullable:true
		uri maxSize:4096
	}

	def setParams(String params="") {
		this.@params = params.substring(0, constraints.params.getAppliedConstraint('maxSize').getMaxSize() - 1)
	}

	def setUri(String uri) {
		this.@uri = uri.substring(0, constraints.uri.getAppliedConstraint('maxSize').getMaxSize() - 1)
	}
}
