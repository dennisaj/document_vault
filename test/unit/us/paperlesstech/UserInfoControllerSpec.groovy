package us.paperlesstech

import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.User

@TestFor(UserInfoController)
@Mock([Group, User, Folder, PinnedFolder])
class UserInfoControllerSpec extends Specification {
	AuthService authService = Mock()
	NotificationService notificationService = Mock()
	TenantService tenantService = Mock()
	User user
	Group group
	Folder folder1

	def setup() {
		controller.authService = authService
		controller.notificationService = notificationService
		controller.tenantService = tenantService

		user = UnitTestHelper.createUser()

		group = new Group(id:1, name:'group1')
		folder1 = new Folder(id:1, name:'test folder', group:group).save(failOnError:true)

		new PinnedFolder(folder:folder1, user:user).save(failOnError:true)

		Folder.metaClass.getFlags = { -> [] }
		Folder.authService = authService
	}

	def cleanup() {
		Folder.metaClass.getFlags = null
	}

	def 'index returns an error if the user is not logged in'() {
		when:
		controller.index()
		def results = JSON.parse(response.contentAsString)

		then:
		results.notification
		1 * authService.authenticatedUser >> null
		1 * notificationService.error(_)
	}

	def 'index returns pinned folders'() {
		when:
		controller.index()
		def results = JSON.parse(response.contentAsString)

		then:
		results.notification
		results.user.pinnedFolders
		results.user.pinnedFolders[0].name == folder1.name
		results.user.pinnedFolders[0].id == folder1.id
		1 * authService.authenticatedUser >> user
		1 * notificationService.success(_)
	}

	def 'index returns an empty flag list if the tenant does not have any'() {
		when:
		controller.index()
		def results = JSON.parse(response.contentAsString)

		then:
		results.notification
		!results.flags
		1 * notificationService.success(_)
		1 * authService.authenticatedUser >> user
		1 * tenantService.getTenantConfigList('flag') >> []
	}

	def 'index returns tenant flags if there are any'() {
		when:
		controller.index()
		def results = JSON.parse(response.contentAsString)

		then:
		results.notification
		results.flags
		results.flags[0] == 'Flag'
		1 * notificationService.success(_)
		1 * authService.authenticatedUser >> user
		1 * tenantService.getTenantConfigList('flag') >> ['Flag']

	}
}
