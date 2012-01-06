package us.paperlesstech

import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import spock.lang.Specification
import us.paperlesstech.nimble.Group

@TestFor(ActivityLogController)
@Mock([ActivityLog, Document, DocumentData, PreviewImage, Group])
class ActivityLogControllerSpec extends Specification {
	def setup() {
		def document = UnitTestHelper.createDocument()
		def document2 = UnitTestHelper.createDocument()
		new ActivityLog(id:1, document:document).save(validate: false, failOnError: true, flush: true)
		new ActivityLog(id:2, document:document).save(validate: false, failOnError: true, flush: true)
		new ActivityLog(id:3, document:document2).save(validate: false, failOnError: true, flush: true)
		new ActivityLog(id:4, document:document2).save(validate: false, failOnError: true, flush: true)
		new ActivityLog(id:5, document:document2).save(validate: false, failOnError: true, flush: true)
	}

	def "list should only default max to 10 and allow only from 1 to 100"() {
		when:
		controller.params.max = actual
		controller.list(null)
		then:
		controller.params.max == expected
		where:
		actual << [null, '0', '-1', '1', '2', '50', '99', '100', '101']
		expected << [10, 1, 1, 1, 2, 50, 99, 100, 100]
	}

	def "index should redirect to list"() {
		when:
		controller.index()
		then:
		response.redirectedUrl == '/activityLog/list'
	}

	def "list should respect documentId"() {
		when:
		def model = controller.list(documentId)
		then:
		model.activityLogInstanceList.size() == size
		model.activityLogInstanceTotal == size
		where:
		documentId << [1L, 2L]
		size << [2, 3]
	}

	def "list should return at most 'max' ActivityLogs when no documentId is passed"() {
		when:
		controller.params.max = max
		def model = controller.list(null)
		then:
		model.activityLogInstanceList.size() == max
		model.activityLogInstanceTotal == ActivityLog.count()
		where:
		max = 3
	}

	def "show should return an error if an invalid log id is passed in"() {
		when:
		controller.show(null)
		then:
		controller.flash.type == 'error'
		response.redirectedUrl == '/activityLog/list'
	}

	def "show should return an ActivityLog if a valid log id is passed in"() {
		when:
		def model = controller.show(id)
		then:
		model.activityLogInstance.id == id
		where:
		id = 1L
	}
}
