package us.paperlesstech.nimble

import grails.plugin.spock.UnitSpec
import us.paperlesstech.AuthService

class GroupServiceSpec extends UnitSpec {
	AuthService authService = Mock()
	GroupService groupService
	User user = new User(id: 1, username: 'user', profile: new Profile(id: 1))
	Group group = new Group(id: 1, name: 'group')

	def setup() {
		mockLogging(GroupService)
		groupService = new GroupService()
		groupService.authService = authService

		mockDomain(Profile, [user.profile])
		mockDomain(User, [user])
		mockDomain(Group, [group])
	}

	def "addMember should call authService's resetCache"() {
		when:
		groupService.addMember(user, group)
		then:
		1 * authService.resetCache(user)
	}

	def "addMember should throw a RunTimeException when saving the user fails"() {
		given:
		user.username = ''
		when:
		groupService.addMember(user, group)
		then:
		thrown(RuntimeException)
	}

	def "addMember should throw a RunTimeException when saving the group fails"() {
		given:
		group.name = ''
		when:
		groupService.addMember(user, group)
		then:
		thrown(RuntimeException)
	}

	def "deleteMember should call authService's resetCache"() {
		when:
		groupService.deleteMember(user, group)
		then:
		1 * authService.resetCache(user)
	}

	def "deleteMember should throw a RunTimeException when saving the user fails"() {
		given:
		user.username = ''
		when:
		groupService.deleteMember(user, group)
		then:
		thrown(RuntimeException)
	}

	def "deleteMember should throw a RunTimeException when saving the group fails"() {
		given:
		group.name = ''
		when:
		groupService.deleteMember(user, group)
		then:
		thrown(RuntimeException)
	}

	def "deleteGroup should call authService's resetCache"() {
		when:
		groupService.deleteGroup(group)
		then:
		1 * authService.resetCache(group)
	}
}
