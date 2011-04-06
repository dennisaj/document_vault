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
	
	def addPrintLog(request, document) {
		return addLog(request, ActivityLog.ActivityType.PRINT, document)
	}
	
	def addSignLog(request, document, signatures) {
		return addLog(request, ActivityLog.ActivityType.SIGN, document, signatures)
	}
	
	def addLog(request, activityType, document, signatures=[:]) {
		def activityLog = new ActivityLog(activityType: activityType,
				userAgent: request.getHeader("User-Agent"),
				ip: request.getRemoteAddr(),
				user: springSecurityService.currentUser,
				pagesAffected: signatures.keySet().join(','),
				document: document)
		
		activityLog.save()
		
		return activityLog
	}
}
