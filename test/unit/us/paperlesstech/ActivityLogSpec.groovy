package us.paperlesstech

import us.paperlesstech.nimble.User
import spock.lang.Specification
import grails.test.mixin.TestFor

@TestFor(ActivityLog)
class ActivityLogSpec extends Specification {
	TenantService tenantService = Mock()
	DomainTenantMap domainTenantMap = Mock()

	def "test map output"() {
		given:
		def doc = new Document()
		doc.id = 42
		def u = new User()
		u.id = 13
		def del = new User()
		del.id = 12

		ActivityLog al = new ActivityLog()
		al.tenantService = tenantService
		al.action = action
		al.delegate = del
		al.document = doc
		al.ip = ip
		al.pageNumber = pageNumber
		al.params = params
		al.status = status
		al.uri = uri
		al.user = u
		al.userAgent = userAgent


		when:
		Map m = al.asMap()

		then:
		1 * tenantService.currentTenant >> domainTenantMap
		1 * domainTenantMap.mappedTenantId >> 42
		m.tenant == 42
		m.action == action
		m.delegate == del.id
		m.document == doc.id
		m.ip == ip
		m.pageNumber == pageNumber
		m.params == params
		m.status == status
		m.uri == uri
		m.user == u.id
		m.userAgent == userAgent

		where:
		action = "action"
		ip = "127.0.0.1"
		pageNumber = "2"
		params = ["param1": "arg1", "param2": "arg2"].toString()
		status = 200
		uri = "/document/foo"
		userAgent = "Chrome or something"
	}
}
