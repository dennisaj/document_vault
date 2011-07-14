package us.paperlesstech

import org.apache.shiro.SecurityUtils

class LogoutController {
	static navigation = [[action: "", isVisible: { authService.isLoggedIn() }, order: 100, title: "Logout"]]
	
	def authService

	def index = {
		// Using the full URL because the load balancer is messing up redirects in production
		def url = g.createLink(absolute:true, controller:'auth',action:'logout')

		redirect(url: url)
	}
}
