public class TimingFilters {
	def grailsApplication

	def filters = {
		timer(controller: "*") {
			before = {
				if (grailsApplication.config.document_vault.timing.enabled) {
					request._timeBeforeRequest = System.currentTimeMillis()
				}
			}

			after = {
				if (grailsApplication.config.document_vault.timing.enabled) {
					request._timeAfterRequest = System.currentTimeMillis()
				}
			}

			afterView = {Exception e->
				if (grailsApplication.config.document_vault.timing.enabled) {
					if (request._timeBeforeRequest && request._timeAfterRequest) {
						def actionDuration = request._timeAfterRequest - request._timeBeforeRequest
						def viewDuration = System.currentTimeMillis() - request._timeAfterRequest
						log.info "Request duration for (${controllerName}/${actionName}): ${actionDuration}ms/${viewDuration}ms"
					} else if (request._timeBeforeRequest) {
						def totalDuration = System.currentTimeMillis() - request._timeBeforeRequest
						log.info "Total duration for (${controllerName}/${actionName}): ${totalDuration}ms"
					}
				}
			}
		}
	}
}
