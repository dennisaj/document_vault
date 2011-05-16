package us.paperlesstech

import org.apache.shiro.SecurityUtils

class LogoutController {
	static navigation = [[action: "", isVisible: { SecurityUtils.subject.authenticated }, order: 100, title: "Logout"]]

    def index = {
		redirect controller: "auth", action: "logout"
	}
}
