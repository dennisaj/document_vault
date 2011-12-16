import us.paperlesstech.filters.ResponseWrapper
import us.paperlesstech.filters.ResponseWrapperFilter

public class LoggingFilters {
	def activityLogService
	def authService

	// This also comes after the Tenant Filter which is a standard java filter included in the web.xml
	def dependsOn = [TimingFilters]

	def filters = {
		all(controller: '*') {
			afterView = {Exception e->
				if (e) {
					log.error(e, e)
				}

				if (authService.isLoggedIn()) {
					ResponseWrapper ptResp = ResponseWrapperFilter.getResponseWrapper(response)
					def status = ptResp?.getPTStatus() ?: 200
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
