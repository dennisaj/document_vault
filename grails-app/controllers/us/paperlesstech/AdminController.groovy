package us.paperlesstech

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

class AdminController {
	static navigation = [[action:'index', isVisible: {SpringSecurityUtils.ifAllGranted 'ROLE_ADMIN'}, order:90, title:'Admin']]

    def index = { }
}
