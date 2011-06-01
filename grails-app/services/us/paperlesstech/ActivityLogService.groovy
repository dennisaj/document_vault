package us.paperlesstech

class ActivityLogService {
	def authService
	def requestService

	static transactional = true

	def addLog(params=[:]) {
		def activityLog = new ActivityLog(
				userAgent: requestService.getHeader("User-Agent"),
				ip: requestService.getRemoteAddr(),
				user: authService.authenticatedUser,
				// Don't log signature line data
				params: params.subMap(params.keySet() - ['lines']).toString(),
				uri: requestService.getRequestURI())

		activityLog.save()

		return activityLog
	}
}
