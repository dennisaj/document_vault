package us.paperlesstech

import grails.plugin.multitenant.core.util.TenantUtils
import grails.plugin.spock.IntegrationSpec
import us.paperlesstech.nimble.AdminsService
import us.paperlesstech.nimble.Role
import us.paperlesstech.nimble.User
import us.paperlesstech.nimble.UserService

class TenantServiceSpec extends IntegrationSpec {
	def tenantService

	def "test tenant initialization"() {
		given:
		def closureCalled = false
		def id = new Random().nextInt()
		def tenant = new DomainTenantMap(name: "testTenant$id", mappedTenantId: id, domainName: "localhost")
		tenant.save()

		when:
		tenantService.initTenant(id, {closureCalled = true})

		then:
		closureCalled
		TenantUtils.doWithTenant(id) {
			assert Role.findByName(UserService.USER_ROLE)
			assert Role.findByName(AdminsService.ADMIN_ROLE)
			assert User.count() == 2
			assert User.findByUsername("user")
			assert User.findByUsername("admin")
		}
	}
}
