package us.paperlesstech

import grails.converters.JSON
import grails.plugin.spock.ControllerSpec
import us.paperlesstech.nimble.User
import us.paperlesstech.nimble.Group

class UserInfoControllerSpec extends ControllerSpec {
	def grailsApplication = [mainContext: new Object()]
	def navigationTagLib = new Object()
	AuthService authService = Mock()
	NotificationService notificationService = Mock()
	User user = Mock()
	Group group = Mock()
	Folder folder1

	def setup() {
		controller.authService = authService
		controller.notificationService = notificationService
		controller.grailsApplication = grailsApplication

		grailsApplication.mainContext.metaClass.getBean = { String beanName -> navigationTagLib }
		navigationTagLib.metaClass.eachItem = { Map map, Closure closure -> }

		folder1 = new Folder(name: 'test folder', group: group)
		folder1.id = 1

		group.id >>> 1
		group.name >>> 'group name'
	}

	def 'index returns an error if the user is not logged in'() {
		when:
		controller.index()
		def results = JSON.parse(mockResponse.contentAsString)

		then:
		results.notification
		1 * authService.authenticatedUser >> null
		1 * notificationService.error(_)
	}

	def 'index returns pinned folders'() {
		when:
		controller.index()
		def results = JSON.parse(mockResponse.contentAsString)

		then:
		results.notification
		results.pinnedFolders
		results.pinnedFolders[0].name == folder1.name
		results.pinnedFolders[0].id == folder1.id
		1 * user.pinnedFolders >> [folder1]
		1 * authService.authenticatedUser >> user
		1 * notificationService.success(_)
	}
}
