package us.paperlesstech

import spock.lang.Specification

class WriteLogsJobSpec extends Specification {
	def "test execute"() {
		given:
		ActivityLogService activityLogService = Mock()
		def job = new WriteLogsJob()
		job.activityLogService = activityLogService

		when:
		job.execute()

		then:
		1 * activityLogService.writeQueuedLogs()
	}
}
