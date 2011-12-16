package us.paperlesstech

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.User
import us.paperlesstech.nimble.Profile

@TestFor(ActivityLogService)
@Mock([ActivityLog, DomainTenantMap, User, Profile, Document, DocumentData, PreviewImage, Group])
class ActivityLogServiceSpec extends Specification {
	AuthService authService = Mock()
	def service = new ActivityLogService()
	RequestService request = Mock()
	Document document
	TenantService tenantService = Mock()

	def setup() {
		service.authService = authService
		service.requestService = request
		service.metaClass.newActivityLog = {->
			ActivityLog activityLog = new ActivityLog()
			activityLog.tenantService = tenantService
			activityLog
		}

		document = UnitTestHelper.createDocument()

		new DomainTenantMap(domainName: 'test', name: 'test', mappedTenantId: 42).save(flush: true, failOnError: true)
		DomainTenantMap.metaClass.withThisTenant = { Closure closure ->
			closure.call()
		}
	}

	def cleanup() {
		DomainTenantMap.metaClass.withThisTenant = null
	}

	def 'add log should call methods off the request service'() {
		def activityLogMap = [:]
		service.metaClass.sendMessage = { Map m -> activityLogMap = m }
		def currentUser = UnitTestHelper.createUser()

		when: 'Try to save'
		service.addLog(controller, action, status, params)

		then:
		1 * tenantService.currentTenant >> DomainTenantMap.list()[0]
		1 * request.getHeader('User-Agent') >> userAgent
		1 * request.getRemoteAddr() >> ip
		1 * request.getRequestURI() >> uri
		1 * authService.authenticatedUser >> currentUser
		activityLogMap.tenant == DomainTenantMap.list()[0].mappedTenantId
		activityLogMap.action == 'document:index'
		activityLogMap.document == document.id
		activityLogMap.ip == ip
		activityLogMap.pageNumber == '2'
		activityLogMap.params == [param1: '1', param2: '2'].toString()
		activityLogMap.status == status
		activityLogMap.user == currentUser.id
		activityLogMap.userAgent == userAgent
		activityLogMap.uri == uri

		where:
		action = null
		controller = 'document'
		status = 200
		userAgent = 'FF'
		ip = '127.0.0.1'
		uri = '/document_vault/document/index'
		params = [param1: '1', param2: '2', documentId: '1', pageNumber: '2', lines: 'blah', password: 'password']
	}

	def "json parsing all fields"() {
		def user = UnitTestHelper.createUser()
		def del = UnitTestHelper.createUser()

		when:
		def al = service.createFromJson("""
{
   "tenant": ${DomainTenantMap.list()[0].mappedTenantId},
   "document": ${document.id},
   "status": 200,
   "userAgent": "Chrome",
   "action": "action",
   "pageNumber": "2",
   "dateCreated": null,
   "params": "{param1=arg1, param2=arg2}",
   "uri": "/document/foo",
   "user": ${user.id},
   "ip": "ip addr",
   "delegate": ${del.id}
}""")

		then:
		al.delegate == del
		al.document == document
		al.status == 200
		al.userAgent == "Chrome"
		al.action == "action"
		al.pageNumber == "2"
		al.dateCreated != null
		al.params == "{param1=arg1, param2=arg2}"
		al.uri == "/document/foo"
		al.user == user
		al.ip == "ip addr"
	}

	def "json no optional fields"() {
		when:
		def al = service.createFromJson("""
{
   "tenant": ${DomainTenantMap.list()[0].mappedTenantId},
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
		when:
		def al = service.createFromJson("""
{
   "tenant": ${DomainTenantMap.list()[0].mappedTenantId},
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
