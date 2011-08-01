package us.paperlesstech

import grails.plugin.spock.UnitSpec

/**
 * Taken from http://www.jworks.nl/2011/08/01/groovy-metaclass-magic-in-unit-tests/
 */
@Category(UnitSpec)
class MetaClassMixin {
	Delegate metaClassFor(Class clazz) {
		registerMetaClass(clazz)

		new Delegate(target: clazz.metaClass)
	}

	Delegate staticMetaClassFor(Class clazz) {
		registerMetaClass(clazz)

		new Delegate(target: clazz.metaClass.'static')
	}

	private static class Delegate {
		Object target

		@Override
		void setProperty(String property, Object newValue) {
			target.setProperty(property, newValue)
		}
	}
}
