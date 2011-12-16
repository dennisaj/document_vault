package us.paperlesstech

class ActivityLogController {
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index() {
		redirect action: "list", params: params
	}

	def list() {
		params.max = Math.max(Math.min(params.max ? params.int('max') : 10, 100), 1)
		def document = Document.get(params.long('documentId'))
		if (document) {
			[activityLogInstanceList: ActivityLog.findAllByDocument(document, params), activityLogInstanceTotal: ActivityLog.countByDocument(document)]
		} else {
			[activityLogInstanceList: ActivityLog.list(params), activityLogInstanceTotal: ActivityLog.count()]
		}
	}

	def show() {
		def activityLogInstance = ActivityLog.get(params.id)
		if (!activityLogInstance) {
			flash.type = "error"
			flash.message = g.message(code: 'default.not.found.message', args: [g.message(code: 'activityLog.label', default: 'ActivityLog'), params.id])
			redirect(action: "list")
		} else {
			[activityLogInstance: activityLogInstance]
		}
	}
}
