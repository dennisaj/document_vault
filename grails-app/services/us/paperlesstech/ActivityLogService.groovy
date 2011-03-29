package us.paperlesstech

class ActivityLogService {
	def springSecurityService
	
    static transactional = true

	def addViewLog(request, document) {
		return addLog(request, ActivityLog.ActivityType.VIEW, document)
	}
	
	def addDeleteLog(request, document, signatures) {
		return addLog(request, ActivityLog.ActivityType.DELETE, signatures)
	}
	
	def addSignLog(request, document, signatures) {
		return addLog(request, ActivityLog.ActivityType.SIGN, document, signatures)
	}
	
	def addLog(request, activityType, document, signatures=[:]) {
		def activityLog = new ActivityLog()
		activityLog.activityType = activityType
		activityLog.userAgent = request.getHeader("User-Agent")
		activityLog.ip = request.getRemoteAddr()
		activityLog.user = springSecurityService.currentUser
		activityLog.pagesAffected = signatures.keySet().join(',')
		activityLog.document = document
		
		activityLog.save()
		
		return activityLog
	}
}
