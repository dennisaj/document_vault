public class LoggingFilters {
	def activityLogService
	def authenticatedService

	def dependsOn = [TenantFilters]

	def filters = {
		all(controller:'*') {
			before = {
				if (authenticatedService.authenticatedUser) {
					activityLogService.addLog(params)
				}
			}
		}
	}
}
