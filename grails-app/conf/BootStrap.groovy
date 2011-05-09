import us.paperlesstech.Printer
import us.paperlesstech.Role
import us.paperlesstech.User
import us.paperlesstech.UserRole

class BootStrap {
	def springSecurityService
	def init = { servletContext ->
		assert new File("/usr/local/bin/pcl6")?.canExecute(), "Cannot execute /usr/local/bin/pcl6"
		assert new File("/usr/local/bin/gs")?.canExecute(), "Cannot execute /usr/local/bin/gs"

		// Disable JAI native acceleration layer
		System.setProperty('com.sun.media.jai.disableMediaLib', 'true')

		if(User.count() == 0) {
			def adminRole = new Role(name: 'ROLE_ADMIN').save(flush: true)
			def userRole = new Role(name: 'ROLE_USER').save(flush: true)
			String adminPassword = springSecurityService.encodePassword('admin')
			String normalPassword = springSecurityService.encodePassword('normal')
			def adminUser = new User(username: 'admin', enabled: true, userPassword: adminPassword)
			def normalUser = new User(username: 'normal', enabled: true, userPassword: normalPassword)
			adminUser.save(flush: true)
			normalUser.save(flush: true)
			UserRole.create adminUser, adminRole, true
			UserRole.create normalUser, userRole, true
		}

		if (Printer.count() == 0) {
			new Printer(name:"Recursive", host:"localhost", deviceType:"lj5gray", port:9100).save()
		}
	}

	def destroy = {
	}
}
