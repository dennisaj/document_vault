package us.paperlesstech

import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.Profile
import us.paperlesstech.nimble.User

class UnitTestHelper {
	static final Random random = new Random()

	static Document createDocument(Map input = [:]) {
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
		document.addToFiles(documentData)
		document.addToPreviewImages(previewImage)
		document.save(flush: true, failOnError: true)

		assert !document.hasErrors()

		document
	}

	static User createUser(Map input = [:]) {
		def username = input.username ?: 'user' + random.nextInt()
		def profile = new Profile()

		def user = new User(username: username, profile: profile)
		user.save(flush: true, failOnError: true)

		assert !user.hasErrors()

		user
	}
}
