package us.paperlesstech


class WriteLogsJob {
	// Every minute at 12 seconds past the minute, attempt to write any pending logs
	static triggers = {
		cron name: 'writeTrigger', cronExpression: '12 * * ? * *'
	}
	def concurrent = false
	def activityLogService

	def execute() {
		log.debug 'Writing pending logs'
		activityLogService.writeQueuedLogs()
	}
}
