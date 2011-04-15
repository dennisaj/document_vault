package us.paperlesstech

class ActivityLogService {
	def springSecurityService

    static transactional = true

	def addViewLog(request, document) {
		return addLog(request, ActivityLog.ActivityType.VIEW, document)
	}

	def addDeleteLog(request, document) {
		return addLog(request, ActivityLog.ActivityType.DELETE, document)
	}

	def addEmailLog(request, document, notes) {
		return addLog(request, ActivityLog.ActivityType.EMAIL, document, notes)
	}

	def addPrintLog(request, document, notes="") {
		return addLog(request, ActivityLog.ActivityType.PRINT, document, notes)
	}

	def addSignLog(request, document, signatures, notes="") {
		return addLog(request, ActivityLog.ActivityType.SIGN, document, notes, signatures)
	}

	def addLog(request, activityType, document, notes="", signatures=[:]) {
		def activityLog = new ActivityLog(activityType: activityType,
				userAgent: request.getHeader("User-Agent"),
				ip: request.getRemoteAddr(),
				user: springSecurityService.currentUser,
				pagesAffected: signatures.keySet().join(','),
				document: document,
				notes: notes)
		
		activityLog.save()

		return activityLog
	}
}
