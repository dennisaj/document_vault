import us.paperlesstech.filters.ResponseWrapper
import us.paperlesstech.filters.ResponseWrapperFilter

public class LoggingFilters {
	def activityLogService
	def authService

	def dependsOn = [TenantFilters]

	def filters = {
		all(controller: '*') {
			afterView = {
				if (authService.isLoggedIn()) {
					ResponseWrapper ptResp = ResponseWrapperFilter.getResponseWrapper(response)
					def status = ptResp.getPTStatus() ?: 200
					def logParams = params.clone()
					if (actionName == "downloadImage" && !logParams.pageNumber) {
						logParams.pageNumber = 1
					}
					activityLogService.addLog(controllerName, actionName, status, logParams)
				}
			}
		}
	}
}
