package us.paperlesstech

import grails.plugin.multitenant.core.util.TenantUtils
import us.paperlesstech.helpers.InstanceGenerator
import us.paperlesstech.nimble.AdminsService
import us.paperlesstech.nimble.Role
import us.paperlesstech.nimble.User
import us.paperlesstech.nimble.UserService

class TenantService {

	static transactional = true

	def adminsService
	def userService

	/** 
	 * This method provides a tenant with the base roles and users it needs.
	 * 
	 * @param closure Optionally, a closure can be provided that will be executed against the given tenantId
	 */
	def initTenant(int tenantId, Closure closure=null) {
		TenantUtils.doWithTenant(tenantId) {
			def userRole = Role.findByName(UserService.USER_ROLE)
			if (!userRole) {
				userRole = new Role()
				userRole.description = 'Issued to all users'
				userRole.name = UserService.USER_ROLE
				userRole.protect = true
				userRole.save()

				if (userRole.hasErrors()) {
					userRole.errors.each {
						log.error it
					}
					throw new RuntimeException("Unable to create valid users role for tenant $tenantId")
				}
			}

			def adminRole = Role.findByName(AdminsService.ADMIN_ROLE)
			if (!adminRole) {
				adminRole = new Role()
				adminRole.description = 'Assigned to users who are considered to be system wide administrators'
				adminRole.name = AdminsService.ADMIN_ROLE
				adminRole.protect = true
				adminRole.save()

				if (adminRole.hasErrors()) {
					adminRole.errors.each {
						log.error it
					}
					throw new RuntimeException("Unable to create valid administrative role for tenant $tenantId")
				}
			}

			if (User.count() == 0) {
				// Create example User account
				def user = InstanceGenerator.user()
				user.username = "user"
				user.pass = 'useR123!'
				user.passConfirm = 'useR123!'
				user.enabled = true

				def userProfile = InstanceGenerator.profile()
				userProfile.fullName = "Test User"
				userProfile.owner = user
				user.profile = userProfile

				def savedUser = userService.createUser(user)
				if (savedUser.hasErrors()) {
					savedUser.errors.each {
						log.error it
					}
					throw new RuntimeException("Error creating example user")
				}

				// Create example Administrative account
				def admin = InstanceGenerator.user()
				admin.username = "admin"
				admin.pass = "admiN123!"
				admin.passConfirm = "admiN123!"
				admin.enabled = true

				def adminProfile = InstanceGenerator.profile()
				adminProfile.fullName = "Administrator"
				adminProfile.owner = admin
				admin.profile = adminProfile

				def savedAdmin = userService.createUser(admin)
				if (savedAdmin.hasErrors()) {
					savedAdmin.errors.each {
						log.error it
					}
					throw new RuntimeException("Error creating administrator")
				}

				adminsService.add(admin)
			}

			if (closure) {
				closure()
			}
		}
	}
}
