package us.paperlesstech

class ActivityLogService {
	static transactional = false

	def authServiceProxy
	def requestService

	/**
	 * Creates a log entry from the given information. params will be altered so this should not be the actual params.
	 *
	 * @param controller The name of the requested controller
	 * @param action the name of the action (defaults to "index")
	 * @param status the http response code
	 * @param params the map of parameters the user sent.  documentId, pageNumber, and lines will be removed
	 *
	 * @return the created log entry
	 */
	ActivityLog addLog(String controller, String action, int status, Map params = [:]) {
		action = action ?: "index"
		def documentId = params.remove("documentId")
		def pageNumber = params.remove("pageNumber")
		// Don't log signature line or document notes data
		params.remove("lines")
		params.remove("notes")

		def activityLog = new ActivityLog(
				action: "$controller:$action",
				delegate: authServiceProxy.delegateUser,
				document: documentId,
				userAgent: requestService.getHeader("User-Agent"),
				ip: requestService.getRemoteAddr(),
				user: authServiceProxy.authenticatedUser,
				pageNumber: pageNumber,
				params: params.toString(),
				status: status,
				uri: requestService.getRequestURI())

		def savedLog = activityLog.save()
		return savedLog ?: activityLog
	}
}
