package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

@MultiTenant
abstract class DocumentField {
	static belongsTo = [document: Document]
	String key
	String value

	static constraints = {
		key(nullable: true, blank: true, unique: "document")
		value(nullable: true, blank: true, maxSize: 4096)
	}

	static mapping = {
		tenantId index: 'document_field_tenant_id_idx'

		key column:"_key"
		value column:"_value"
		tablePerHierarchy(false)
	}

	@Override
	String toString() {
		"DocumentField($key, $value)"
	}
}
