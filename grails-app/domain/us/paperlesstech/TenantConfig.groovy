package us.paperlesstech

import grails.plugin.multitenant.core.annotation.MultiTenant

@MultiTenant
class TenantConfig {
	String key
	String value

	static constraints = {
		key nullable: false, blank: false
		value nullable: true, blank: true, maxSize: 4096
	}

	static mapping = {
		tenantId index: 'tenant_config_tenant_id_idx'

		key column: "_key"
		value column: "_value"
	}

	@Override
	String toString() {
		"TenantConfig($key, $value)"
	}
}
