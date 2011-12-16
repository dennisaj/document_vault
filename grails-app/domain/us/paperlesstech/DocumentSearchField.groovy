package us.paperlesstech

import grails.plugin.multitenant.core.annotation.MultiTenant

@MultiTenant
class DocumentSearchField extends AbstractField {
	static belongsTo = [document: Document]

	static constraints = {
		key(nullable: true, blank: true, unique: "document")
		value(nullable: true, blank: true, maxSize: 4096)
	}

	static mapping = {
		tenantId index: 'document_search_field_tenant_id_idx'

		key column:"_key"
		value column:"_value"
		tablePerHierarchy(false)
	}
}
