package us.paperlesstech

import us.paperlesstech.nimble.User
import grails.plugin.multitenant.core.annotation.MultiTenant

@MultiTenant
class Preference {
	String key
	String value

	static belongsTo = [user:User]

	static constraints = {
		key nullable: false, blank: false, unique: "user"
		value nullable: true, blank: true, maxSize: 4096
	}

	static mapping = {
		tenantId index: 'preference_tenant_id_idx'

		key column:"_key"
		value column:"_value"
	}

	@Override
	String toString() {
		"Preference($user, $key, $value)"
	}
}
