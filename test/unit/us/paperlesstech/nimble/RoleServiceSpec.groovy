package us.paperlesstech.nimble

import grails.plugin.spock.UnitSpec
import us.paperlesstech.AuthService

class RoleServiceSpec extends UnitSpec {
	AuthService authService = Mock()
	RoleService roleService
	User user = new User(id: 1, username: 'user', profile: new Profile(id: 1))
	Role role = new Role(id: 1, name: 'role')
	Group group = new Group(id: 1, name: 'group')

	def setup() {
		mockLogging(RoleService)
		roleService = new RoleService()
		roleService.authService = authService

		mockDomain(Profile, [user.profile])
		mockDomain(User, [user])
		mockDomain(Role, [role])
		mockDomain(Group, [group])
	}

	def "addMember should call authService's resetCache"() {
		when:
		roleService.addMember(user, role)
		then:
		1 * authService.resetCache(user)
	}

	def "addMember should throw a RunTimeException when saving the user fails"() {
		given:
		user.username = ''
		when:
		roleService.addMember(user, role)
		then:
		thrown(RuntimeException)
	}

	def "addMember should throw a RunTimeException when saving the role fails"() {
		given:
		role.name = ''
		when:
		roleService.addMember(user, role)
		then:
		thrown(RuntimeException)
	}

	def "deleteMember should call authService's resetCache"() {
		when:
		roleService.deleteMember(user, role)
		then:
		1 * authService.resetCache(user)
	}

	def "deleteMember should throw a RunTimeException when saving the user fails"() {
		given:
		user.username = ''
		when:
		roleService.deleteMember(user, role)
		then:
		thrown(RuntimeException)
	}

	def "deleteMember should throw a RunTimeException when saving the role fails"() {
		given:
		role.name = ''
		when:
		roleService.deleteMember(user, role)
		then:
		thrown(RuntimeException)
	}

	def "deleteRole should call authService's resetCache"() {
		when:
		roleService.deleteRole(role)
		then:
		1 * authService.resetCache(role)
	}

	def "addGroupMember should call authService's resetCache for both the role and the group"() {
		when:
		roleService.addGroupMember(group, role)
		then:
		1 * authService.resetCache(role)
		1 * authService.resetCache(group)
	}

	def "addGroupMember should throw a RunTimeException when saving the role fails"() {
		given:
		role.name = ''
		when:
		roleService.addGroupMember(group, role)
		then:
		thrown(RuntimeException)
	}

	def "addGroupMember should throw a RunTimeException when saving the group fails"() {
		given:
		role.name = ''
		when:
		roleService.addGroupMember(group, role)
		then:
		thrown(RuntimeException)
	}

	def "deleteGroupMember should call authService's resetCache for both the role and the group"() {
		when:
		roleService.deleteGroupMember(group, role)
		then:
		1 * authService.resetCache(role)
		1 * authService.resetCache(group)
	}

	def "deleteGroupMember should throw a RunTimeException when saving the group fails"() {
		given:
		group.name = ''
		when:
		roleService.deleteGroupMember(group, role)
		then:
		thrown(RuntimeException)
	}

	def "deleteGroupMember should throw a RunTimeException when saving the role fails"() {
		given:
		role.name = ''
		when:
		roleService.deleteGroupMember(group, role)
		then:
		thrown(RuntimeException)
	}
}
