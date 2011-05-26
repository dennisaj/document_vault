package us.paperlesstech

class ActivityLogService {
	def authenticatedService
	def requestService

	static transactional = true

	def addLog(params=[:]) {
		def paramsString = params.toString()
		def uriString = requestService.getRequestURI()
		paramsString = paramsString.substring(0, Math.min(paramsString.length(), 4095))
		uriString = uriString.substring(0, Math.min(uriString.length(), 4095))
		def activityLog = new ActivityLog(
				userAgent: requestService.getHeader("User-Agent"),
				ip: requestService.getRemoteAddr(),
				user: authenticatedService.authenticatedUser,
				params: paramsString,
				uri: uriString)

		activityLog.save()

		return activityLog
	}
}