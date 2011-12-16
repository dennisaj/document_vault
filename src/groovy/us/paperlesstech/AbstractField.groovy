package us.paperlesstech

import grails.plugin.multitenant.core.annotation.MultiTenant

@MultiTenant
abstract class AbstractField {
	String key
	String value

	@Override
	String toString() {
		"Field($key, $value)"
	}
}
