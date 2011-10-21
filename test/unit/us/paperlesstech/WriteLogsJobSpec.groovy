package us.paperlesstech

import grails.plugin.spock.UnitSpec

class WriteLogsJobSpec extends UnitSpec {
	def "test execute"() {
		given:
		mockLogging(WriteLogsJob)
		ActivityLogService activityLogService = Mock()
		def job = new WriteLogsJob()
		job.activityLogService = activityLogService

		when:
		job.execute()

		then:
		1 * activityLogService.writeQueuedLogs()
	}
}
