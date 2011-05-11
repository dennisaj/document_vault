import us.paperlesstech.Printer
import us.paperlesstech.User

class BootStrap {
	def shiroSecurityService

	def init = { servletContext ->
		assert new File("/usr/local/bin/pcl6")?.canExecute(), "Cannot execute /usr/local/bin/pcl6"
		assert new File("/usr/local/bin/gs")?.canExecute(), "Cannot execute /usr/local/bin/gs"

		if (User.count() == 0) {
			def adminUser = new User(username: "admin", passwordHash: shiroSecurityService.encodePassword("admin"))
			adminUser.addToPermissions("*")
			adminUser.save()
		}

		// Disable JAI native acceleration layer
		System.setProperty('com.sun.media.jai.disableMediaLib', 'true')


		if (Printer.count() == 0) {
			new Printer(name:"Recursive", host:"localhost", deviceType:"lj5gray", port:9100).save()
		}
	}

	def destroy = {
	}
}
