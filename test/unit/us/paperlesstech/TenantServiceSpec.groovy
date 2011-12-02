package us.paperlesstech

import grails.plugin.spock.UnitSpec

class TenantServiceSpec extends UnitSpec {
	def service = new TenantService()

	def setup() {
		def list = [new TenantConfig(key: 'flag', value: 'v1'),
				new TenantConfig(key: 'flag', value: 'v2'),
				new TenantConfig(key: 'flag', value: 'v3'),
				new TenantConfig(key: 'flag', value: 'v4')]
		mockDomain(TenantConfig, list)
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
