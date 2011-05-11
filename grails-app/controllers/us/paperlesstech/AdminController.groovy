package us.paperlesstech

import org.apache.shiro.SecurityUtils

class AdminController {
	static navigation = [[action:'index', isVisible: {SecurityUtils.subject.isPermitted("admin:*")}, order:90, title:'Admin']]

    def index = { }
}
