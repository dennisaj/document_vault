package us.paperlesstech.nimble

import org.apache.shiro.SecurityUtils

class AdminController {
	static navigation = [[group: "user", action:'index', isVisible: {SecurityUtils.subject.isPermitted("admin:*")}, order:90, title:'Admin']]

	def index = {
		redirect (controller:"user", action:"list")
	}
}
