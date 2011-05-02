import us.paperlesstech.Printer
import us.paperlesstech.Role
import us.paperlesstech.User
import us.paperlesstech.UserRole

class BootStrap {
	def springSecurityService
	def init = { servletContext ->
		println "All users ${User.list(max: 100)}"
		println "Admin ${User.findByUsername("admin")}"
		println "Authorities ${User.findByUsername("admin")?.getAuthorities()}"

		if (!checkForPcl6()) {
			throw new RuntimeException()
		}

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

	def checkForPcl6 = {
		def file = new File('/usr/local/bin/pcl6')
		return file?.exists() && file?.canRead() && file?.canExecute()
	}

	def destroy = {
	}
}
