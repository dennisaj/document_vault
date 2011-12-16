package us.paperlesstech

import spock.lang.Specification
import grails.test.mixin.TestFor
import grails.test.mixin.Mock

@TestFor(TenantService)
@Mock(TenantConfig)
class TenantServiceSpec extends Specification {
	def service = new TenantService()

	def setup() {
		new TenantConfig(key: 'flag', value: 'v1').save(flush: true, failOnError: true)
		new TenantConfig(key: 'flag', value: 'v2').save(flush: true, failOnError: true)
		new TenantConfig(key: 'flag', value: 'v3').save(flush: true, failOnError: true)
		new TenantConfig(key: 'flag', value: 'v4').save(flush: true, failOnError: true)
	}

	def 'getTenantConfigList returns an empty list if not match'() {
		expect:
		service.getTenantConfigList('nonexistant') == []
	}

	def 'getTenantConfigList returns a list ordered by value'() {
		expect:
		service.getTenantConfigList('flag') == ['v1', 'v2', 'v3', 'v4']
	}
}
