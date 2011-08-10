package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

import java.util.Date

import us.paperlesstech.nimble.User

@MultiTenant
class ActivityLog {
	String action
	Date dateCreated
	String document
	String ip
	String pageNumber
	String params
	int status
	String uri
	User user
	String userAgent

	static mapping = {
		document index: "activity_log_document_idx"
	}

	static constraints = {
		document nullable: true, blank: true
		pageNumber nullable: true, blank: true
		params nullable:true, blank:true, maxSize:4096
		user nullable:true
		uri maxSize:4096
	}

	void setParams(String params="") {
		this.@params = params.substring(0, Math.min(params.length(), 4096 - 1))
	}

	void setUri(String uri) {
		this.@uri = uri.substring(0, Math.min(uri.length(), 4096 - 1))
	}
}
