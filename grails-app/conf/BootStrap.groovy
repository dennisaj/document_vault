import us.paperlesstech.Role
import us.paperlesstech.User
import us.paperlesstech.UserRole

class BootStrap {
	def springSecurityService
	def init = { servletContext ->
		println "All users ${User.list(max: 100)}"
		println "Admin ${User.findByUsername("admin")}"
		println "Authorities ${User.findByUsername("admin")?.getAuthorities()}"
		if(User.count() == 0) {
			def adminRole = new Role(name: 'ROLE_ADMIN').save(flush: true)
			def userRole = new Role(name: 'ROLE_USER').save(flush: true)
			String password = springSecurityService.encodePassword('admin')
			def adminUser = new User(username: 'admin', enabled: true, userPassword: password)
			adminUser.save(flush: true)
			UserRole.create adminUser, adminRole, true
		}
	}
	def destroy = {
	}
}
