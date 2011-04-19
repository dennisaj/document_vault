package us.paperlesstech

class ActivityLogService {
	def requestService
	def springSecurityService

    static transactional = true

	def addViewLog(document) {
		return addLog(ActivityLog.ActivityType.VIEW, document)
	}

	def addDeleteLog(document) {
		return addLog(ActivityLog.ActivityType.DELETE, document)
	}

	def addEmailLog(document, notes) {
		return addLog(ActivityLog.ActivityType.EMAIL, document, notes)
	}

	def addPrintLog(document, notes="") {
		return addLog(ActivityLog.ActivityType.PRINT, document, notes)
	}

	def addSignLog(document, signatures, notes="") {
		return addLog(ActivityLog.ActivityType.SIGN, document, notes, signatures)
	}

	def addLog(activityType, document, notes="", signatures=[:]) {
		def activityLog = new ActivityLog(activityType: activityType,
				userAgent: requestService.getHeader("User-Agent"),
				ip: requestService.getRemoteAddr(),
				user: springSecurityService.currentUser,
				pagesAffected: signatures.keySet().join(','),
				document: document,
				notes: notes)
		
		activityLog.save()

		return activityLog
	}
}