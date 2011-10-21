package us.paperlesstech

import grails.plugin.spock.UnitSpec
import us.paperlesstech.nimble.User

class ActivityLogSpec extends UnitSpec {
	def "test map output"() {
		given:
		def doc = new Document()
		doc.id = 42
		def u = new User()
		u.id = 13
		def del = new User()
		del.id = 12

		ActivityLog al = new ActivityLog()
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
		m.tenant == 0
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
