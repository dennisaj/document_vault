package us.paperlesstech

import grails.plugin.spock.IntegrationSpec
import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.User
import us.paperlesstech.nimble.Profile
import us.paperlesstech.nimble.Permission

class AbstractMultiTenantIntegrationSpec extends IntegrationSpec {
	static def currentTenant
	static final Random random = new Random()

	def setupSpec() {
		currentTenant.set(Integer.valueOf(1))
	}

	def cleanupSpec() {
		currentTenant.set(null)
	}

	Document createDocument(Map input = [:]) {
		AuthService authService = input.authService
		def documentName = input.documentName ?: 'documentName' + random.nextInt()
		def documentDateCreated = input.documentDateCreated ?: new Date()
		def groupName = input.groupName ?: 'groupName' + random.nextInt()
		def group = input.group ?: (new Group(name: groupName).save(flush: true, failOnError: true))
		def pageNumber = input.pageNumber ?: 1
		def documentData = input.documentData ?: new DocumentData(fileKey: 'fileKey' + random.nextInt(), fileSize: 1, mimeType: MimeType.PDF, pages: 1).save(flush: true, failOnError: true)
		Thread.sleep(2);
		def previewImageDD = input.previewImageDD ?: new DocumentData(fileKey: 'fileKey' + random.nextInt(), fileSize: 1, mimeType: MimeType.PNG, pages: 1).save(flush: true, failOnError: true)
		Thread.sleep(2);

		def previewImage = new PreviewImage(data: previewImageDD, pageNumber: pageNumber, thumbnail: previewImageDD)
		def document = new Document(name: documentName,
				dateCreated: documentDateCreated,
				group: group)

		// I am unsure why this has to be set on the instance and the Domain class, but it does or else save will fail
		if (authService) {
			Document.authService = authService
			document.authService = authService
		}

		document.addToFiles(documentData)
		document.addToPreviewImages(previewImage)
		document.save(flush: true, failOnError: true)

		assert !document.hasErrors()

		document
	}

	User createUser(Map input = [:]) {
		def username = input.username ?: 'user' + random.nextInt()
		def permissionStrings = input.permissions

		def profile = new Profile()
		def user = new User(username: username, profile: profile)

		permissionStrings?.each { permissionString ->
			def permission = new Permission(managed: true, type: Permission.defaultPerm, target: permissionString)
			permission.save(flush: true)
			user.addToPermissions(permission)
			user.save(flush: true)
		}

		user.save(flush: true, failOnError: true)

		assert !user.hasErrors()

		user
	}
}
