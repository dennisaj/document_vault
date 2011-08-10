package us.paperlesstech

class PrinterController {
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		[printerInstanceList: Printer.list(params), printerInstanceTotal: Printer.count()]
	}

	def create = {
		def printerInstance = new Printer()
		printerInstance.properties = params
		return [printerInstance: printerInstance]
	}

	def save = {
		def printerInstance = new Printer(params)
		if (printerInstance.save(flush: true)) {
			flash.type = "success"
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'printer.label', default: 'Printer'), printerInstance.id])}"
			redirect(action: "show", id: printerInstance.id)
		} else {
			render(view: "create", model: [printerInstance: printerInstance])
		}
	}

	def show = {
		def printerInstance = Printer.get(params.id)
		if (!printerInstance) {
			flash.type = "error"
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'printer.label', default: 'Printer'), params.id])}"
			redirect(action: "list")
		} else {
			[printerInstance: printerInstance]
		}
	}

	def edit = {
		def printerInstance = Printer.get(params.id)
		if (!printerInstance) {
			flash.type = "error"
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'printer.label', default: 'Printer'), params.id])}"
			redirect(action: "list")
		} else {
			[printerInstance: printerInstance]
		}
	}

	def update = {
		def printerInstance = Printer.get(params.id)
		if (printerInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (printerInstance.version > version) {
					printerInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'printer.label', default: 'Printer')] as Object[], "Another user has updated this Printer while you were editing")
					render(view: "edit", model: [printerInstance: printerInstance])
					return
				}
			}
			printerInstance.properties = params
			if (!printerInstance.hasErrors() && printerInstance.save(flush: true)) {
				flash.type = "success"
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'printer.label', default: 'Printer'), printerInstance.id])}"
				redirect(action: "show", id: printerInstance.id)
			} else {
				render(view: "edit", model: [printerInstance: printerInstance])
			}
		} else {
			flash.type = "error"
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'printer.label', default: 'Printer'), params.id])}"
			redirect(action: "list")
		}
	}

	def delete = {
		def printerInstance = Printer.get(params.id)
		if (printerInstance) {
			try {
				printerInstance.delete(flush: true)
				flash.type = "success"
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'printer.label', default: 'Printer'), params.id])}"
				redirect(action: "list")
			} catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.type = "error"
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'printer.label', default: 'Printer'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		} else {
			flash.type = "error"
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'printer.label', default: 'Printer'), params.id])}"
			redirect(action: "list")
		}
	}
}
