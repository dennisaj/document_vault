package us.paperlesstech

import grails.plugin.multitenant.core.Tenant

/**
 * Maps domain name to tenantId
 */
class DomainTenantMap implements Tenant {
	String domainName
	Integer mappedTenantId
	String name

	static constraints = {}

	Integer tenantId() {
		mappedTenantId
	}
}
