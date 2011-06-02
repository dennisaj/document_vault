package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

@MultiTenant
class DocumentField {
	static belongsTo = [document: Document]
	String key
	String value
	String fieldType = "DocumentField"

	static constraints = {
		key(nullable: true, blank: true, unique: ["document", "fieldType"])
		value(nullable: true, blank: true, maxSize: 4096)
	}

	static mapping = {
		discriminator(formula: "case when field_type='DocumentOtherField' then 1 when field_type='DocumentSearchField' then 2 else 0 end",
				value: "0", type: "integer")
	}

	@Override
	String toString() {
		"DocumentField($key, $value)"
	}
}


