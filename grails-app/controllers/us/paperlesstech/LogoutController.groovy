package us.paperlesstech

import org.apache.shiro.SecurityUtils

class LogoutController {
	static navigation = [[action: "", isVisible: { authService.isLoggedIn() }, order: 100, title: "Logout"]]
	
	def authService

	def index = {
		redirect controller: "auth", action: "logout"
	}
}
