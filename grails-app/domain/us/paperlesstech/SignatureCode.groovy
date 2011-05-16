package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

@MultiTenant
class SignatureCode {
	String email
	String code

	static belongsTo = [document:Document]

	public SignatureCode() {
		code = UUID.randomUUID().toString()
	}

	static constraints = {
		email email:true, blank:false, unique:'document'
		code blank:false, size:36..36, unique:true
	}
}
