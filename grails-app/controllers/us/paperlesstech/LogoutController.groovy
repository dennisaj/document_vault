package us.paperlesstech
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

class LogoutController {
	static navigation = [[action:'index', isVisible: {springSecurityService.isLoggedIn()}, order:100, title:'Logout']]
	
	def springSecurityService
	
	/**
	 * Index action. Redirects to the Spring security logout uri.
	 */
	def index = {
		// TODO  put any pre-logout code here
		redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl // '/j_spring_security_logout'
	}
}
