package us.paperlesstech

import grails.plugin.spock.ControllerSpec

class ActivityLogControllerSpec extends ControllerSpec {
	def setup() {
		controller.metaClass.message = { LinkedHashMap arg1 -> 'this is stupid' }
		def document = new Document(id:1)
		def document2 = new Document(id:2)
		def al1 = new ActivityLog(id:1, document:document)
		def al2 = new ActivityLog(id:2, document:document)
		def al3 = new ActivityLog(id:3, document:document2)
		def al4 = new ActivityLog(id:4, document:document2)
		def al5 = new ActivityLog(id:5, document:document2)
		mockDomain(ActivityLog, [al1, al2, al3, al4, al5])
		mockDomain(Document, [document, document2])
	}

	def "index should redirect to list"() {
		when:
		controller.index()
		then:
		controller.redirectArgs.action == 'list'
	}

	def "list should only default max to 10 and allow only from 1 to 100"() {
		when:
		controller.params.max = actual
		controller.list()
		then:
		controller.params.max == expected
		where:
		actual << [null, '0', '-1', '1', '2', '50', '99', '100', '101']
		expected << [10, 1, 1, 1, 2, 50, 99, 100, 100]
	}

	def "list should respect documentId"() {
		when:
		controller.params.documentId = documentId
		def model = controller.list()
		then:
		model.activityLogInstanceList.size() == size
		model.activityLogInstanceTotal == size
		where:
		documentId << ['1', '2']
		size << [2, 3]
	}

	def "list should return at most 'max' ActivityLogs when no documentId is passed"() {
		when:
		controller.params.max = max
		def model = controller.list()
		then:
		model.activityLogInstanceList.size() == max
		model.activityLogInstanceTotal == ActivityLog.count()
		where:
		max = 3
	}

	def "show should return an error if an invalid log id is passed in"() {
		when:
		controller.show()
		then:
		controller.flash.type == 'error'
		controller.flash.message
		controller.redirectArgs.action == 'list'
	}

	def "show should return an ActivityLog if a valid log id is passed in"() {
		when:
		controller.params.id = id
		def model = controller.show()
		then:
		model.activityLogInstance.id == id
		where:
		id = 1
	}
}
