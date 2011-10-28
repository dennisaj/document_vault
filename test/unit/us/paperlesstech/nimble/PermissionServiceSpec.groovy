package us.paperlesstech.nimble

import grails.plugin.spock.UnitSpec
import spock.lang.Shared
import us.paperlesstech.AuthService

class PermissionServiceSpec extends UnitSpec {
	AuthService authService = Mock()
	PermissionService permissionService
	Permission permission = new Permission(id: 1, target: 'target:*', type: Permission.defaultPerm)
	@Shared
	User user = new User(id: 1, username: 'user', profile: new Profile(id: 1))
	@Shared
	Group group = new Group(id: 1, name: 'group')
	@Shared
	Role role = new Role(id: 1, name: 'role')

	def setup() {
		mockLogging(PermissionService)
		permissionService = new PermissionService()
		permissionService.authService = authService

		mockDomain(Profile, [user.profile])
		mockDomain(Permission, [permission])
		mockDomain(User, [user])
		mockDomain(Role, [role])
		mockDomain(Group, [group])
	}

	def "createPermission should call authService's resetCache"() {
		when:
		permissionService.createPermission(permission, owner)
		then:
		1 * authService.resetCache(owner)
		where:
		owner << [user, group, role]
	}

	def "createPermission should throw a RunTimeException when saving the owner fails"() {
		given:
		permission.type = ''
		when:
		permissionService.createPermission(permission, owner)
		then:
		thrown(RuntimeException)
		where:
		owner << [user, group, role]
	}

	def "deletePermission should call authService's resetCache"() {
		given:
		permission.owner = owner
		owner.addToPermissions(permission)
		when:
		permissionService.deletePermission(permission)
		then:
		1 * authService.resetCache(owner)
		where:
		owner << [user, group, role]
	}

	def "deletePermission should throw a RunTimeException when saving the owner fails"() {
		given:
		user.username = ''
		when:
		permissionService.deletePermission(permission)
		then:
		thrown(RuntimeException)
	}
}
