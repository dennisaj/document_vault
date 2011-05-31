package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

@MultiTenant
class Printer {
	String host
	String deviceType
	String name
	int port

	static constraints = {
		deviceType blank:false
		host blank:false
		name blank:false, unique:"tenantId"
		port range:0..65535
	}

	@Override
	String toString() {
		"Printer (${id}) - ${name} - ${host}:${port}"
	}
}
