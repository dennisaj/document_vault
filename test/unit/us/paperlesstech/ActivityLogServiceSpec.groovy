package us.paperlesstech

import grails.plugin.multitenant.core.CurrentTenant
import grails.plugin.multitenant.core.util.TenantUtils
import grails.plugin.spock.UnitSpec
import us.paperlesstech.nimble.User

class ActivityLogServiceSpec extends UnitSpec {
	AuthService authService = Mock()
	def service = new ActivityLogService()
	RequestService request = Mock()
	def activityMock
	CurrentTenant currentTenant = Mock()

	def setup() {
		service.authService = authService
		service.requestService = request
		activityMock = mockFor(ActivityLog.class)
		TenantUtils.currentTenant = currentTenant
	}

	def cleanup() {
		TenantUtils.currentTenant = null
	}

	def "add log should call methods off the request service"() {
		mockDomain(ActivityLog)
		mockDomain(Document, [new Document(id: 4)])
		def activityLog = null
		service.metaClass.sendMessage = { Map m -> activityLog = m }

		when: "Try to save"
		service.addLog(controller, action, status, params)

		then:
		1 * request.getHeader("User-Agent") >> userAgent
		1 * request.getRemoteAddr() >> ip
		1 * request.getRequestURI() >> uri
		1 * authService.authenticatedUser >> currentUser
		activityLog.action == "document:index"
		activityLog.document == 4
		activityLog.ip == ip
		activityLog.pageNumber == "2"
		activityLog.params == [param1: "1", param2: "2"].toString()
		activityLog.status == status
		activityLog.user == currentUser.id
		activityLog.userAgent == userAgent
		activityLog.uri == uri

		where:
		action = null
		controller = "document"
		status = 200
		userAgent = "FF"
		ip = "127.0.0.1"
		currentUser = new User()
		uri = "/document_vault/document/index"
		params = [param1: "1", param2: "2", documentId: "4", pageNumber: "2", lines: "blah", password: 'password']
	}

	def "json parsing all fields"() {
		given:
		def d = new Document()
		d.id = 42
		mockDomain(Document, [d])
		def u = new User()
		u.id = 13
		def del = new User()
		del.id = 12
		mockDomain(User, [u, del])
		mockDomain(ActivityLog)

		when:
		def al = service.createFromJson("""
{
   "tenant": 0,
   "document": 42,
   "status": 200,
   "userAgent": "Chrome",
   "action": "action",
   "pageNumber": "2",
   "dateCreated": null,
   "params": "{param1=arg1, param2=arg2}",
   "uri": "/document/foo",
   "user": 13,
   "ip": "ip addr",
   "delegate": 12
}""")

		then:
		al.delegate == del
		al.document == d
		al.status == 200
		al.userAgent == "Chrome"
		al.action == "action"
		al.pageNumber == "2"
		al.dateCreated != null
		al.params == "{param1=arg1, param2=arg2}"
		al.uri == "/document/foo"
		al.user == u
		al.ip == "ip addr"
	}

	def "json no optional fields"() {
		mockDomain(ActivityLog)

		when:
		def al = service.createFromJson("""
{
   "tenant": 0,
   "status": 200,
   "userAgent": "Chrome",
   "action": "action",
   "dateCreated": null,
   "uri": "/document/foo",
   "ip": "ip addr",
}""")

		then:
		al.dateCreated != null
		al.delegate == null
		al.document == null
		al.status == 200
		al.userAgent == "Chrome"
		al.action == "action"
		al.pageNumber == null
		al.params == null
		al.uri == "/document/foo"
		al.user == null
		al.ip == "ip addr"
	}

	def "json with null optional fields"() {
		mockDomain(ActivityLog)

		when:
		def al = service.createFromJson("""
{
   "tenant": 0,
   "document": null,
   "status": 200,
   "userAgent": "Chrome",
   "action": "action",
   "pageNumber": null,
   "dateCreated": null,
   "params": null,
   "uri": "/document/foo",
   "user": null,
   "ip": "ip addr",
   "delegate": null
}""")

		then:
		al.dateCreated != null
		al.delegate == null
		al.document == null
		al.status == 200
		al.userAgent == "Chrome"
		al.action == "action"
		al.pageNumber == null
		al.params == null
		al.uri == "/document/foo"
		al.user == null
		al.ip == "ip addr"
	}
}
