package us.paperlesstech

class ActivityLogController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		[activityLogInstanceList: ActivityLog.list(params), activityLogInstanceTotal: ActivityLog.count()]
	}

	def show = {
		def activityLogInstance = ActivityLog.get(params.id)
		if (!activityLogInstance) {
			flash.type = "error"
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'activityLog.label', default: 'ActivityLog'), params.id])}"
			redirect(action: "list")
		} else {
			[activityLogInstance: activityLogInstance]
		}
	}
}
