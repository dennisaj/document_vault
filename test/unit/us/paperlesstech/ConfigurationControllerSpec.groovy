package us.paperlesstech

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(ConfigurationController)
@Mock([TenantConfig])
class ConfigurationControllerSpec extends Specification {
	def tenantService

	def setup() {
		new TenantConfig(key: 'flag', value: 'test flag').save(flush: true, failOnError: true)

		tenantService = Mock(TenantService)
		tenantService.getTenantConfigList('flag') >> { TenantConfig.list()*.value }

		controller.tenantService = tenantService
	}

	def "test index"() {
		when:
		controller.index()

		then:
		assert view == "/configuration/index"
	}

	def "addFlag throws an error if the flag is not passed"() {
		when:
		controller.addFlag(' ')

		then:
		thrown(AssertionError)
	}

	def "addFlag throws an error if the flag already exists"() {
		when:
		controller.addFlag('test flag')

		then:
		thrown(AssertionError)
	}

	def "addFlag adds new flags"() {
		when:
		controller.addFlag('new flag')

		then:
		assert response.text.contains('new flag')
	}
	
	def "list flags returns the flags"() {
		expect:
		controller.listFlags().flags == TenantConfig.list()*.value
	}

	def "removeFlag throws an error if the flag is not passed"() {
		when:
		controller.removeFlag(' ')

		then:
		thrown(AssertionError)
	}

	def "removeFlag throws an error if the flag does not exist"() {
		when:
		controller.removeFlag('does not exist')

		then:
		thrown(AssertionError)
	}

	def "removeFlag removes flags"() {
		when:
		controller.removeFlag('test flag')

		then:
		assert !response.text.contains('test flag')
	}
}
