package us.paperlesstech

import us.paperlesstech.nimble.Group

class DomainIntegrationSpec extends AbstractMultiTenantIntegrationSpec {
	def authService
	def user
	def sessionFactory
	def previewImageData
	def fileData
	def group
	def firstDateCreated = new GregorianCalendar(2011, 1, 1).time
	def secondDateCreated = new GregorianCalendar(2010, 1, 1).time

	def setup() {
		user = createUser()
		authService = Mock(AuthService)
		authService.authenticatedUser >>> user
		Document.authService = authService
		Folder.authService = authService

		previewImageData = new DocumentData(mimeType: MimeType.PDF, fileKey: "previewImageDataKey", fileSize: 1,
				dateCreated: firstDateCreated)
		previewImageData.save(failOnError: true)
		fileData = new DocumentData(mimeType: MimeType.PDF, fileKey: "fileDataKey", fileSize: 1,
				dateCreated: secondDateCreated)
		fileData.save(failOnError: true)
		group = getGroup()
	}

	static def getGroup() {
		def g = new Group(name: "test " + Math.random())
		g.save()
		g
	}

	def getFolder() {
		def f = new Folder(name: 'folder name', group: group)
		f.authService = authService
		
		f
	}

	def "can add to document maps"() {
		given:
		assert Document.count() == 0
		assert DocumentSearchField.count() == 0
		assert DocumentOtherField.count() == 0

		when:
		def d = createDocument(authService: authService)
		d.searchField("field1", "value1")
		d.otherField("field1", "value2")
		def result = d.save(failOnError: true, flush:true)

		then: "Adding to the document map it should not throw a NPE"
		result
		Document.count() == 1
		DocumentSearchField.count() == 1
		DocumentOtherField.count() == 1
		result.searchField("field1") == "value1"
		result.otherField("field1") == "value2"
	}

	def "can update document maps"() {
		given:
		assert Document.count() == 0
		assert DocumentSearchField.count() == 0
		assert DocumentOtherField.count() == 0
		def d = createDocument(authService: authService)
		d.searchField("field1", "value1")
		d.otherField("field1","value2")
		assert d.save(failOnError: true)

		when:
		d = Document.get(d.id)
		d.searchField("field1", "value3")
		d.otherField("field1","value4")
		def result = d.save(failOnError: true)
		d = Document.get(d.id)

		then: "Adding to the document map it should not throw a NPE"
		result
		Document.count() == 1
		DocumentSearchField.count() == 1
		DocumentOtherField.count() == 1
		d.searchField("field1") == "value3"
		d.otherField("field1") == "value4"
	}

	def "you can save a document without a preview image"() {
		given:
		assert PreviewImage.count() == 0
		assert Document.count() == 0

		when:
		def d = createDocument(authService: authService)
		def images = new ArrayList(d.previewImages)
		images.each {
			d.removeFromPreviewImages(it)
			it.delete(failOnError: true, flush: true)
		}
		d.save(failOnError: true, flush: true)

		then:
		PreviewImage.count() == 0
		Document.count() == 1
	}

	def "saving a document cascades to a preview image"() {
		given:
		assert PreviewImage.count() == 0
		assert Document.count() == 0

		when:
		def d = createDocument(authService: authService)
		def i = new PreviewImage(data: previewImageData, thumbnail: previewImageData, pageNumber:2)
		d.addToPreviewImages(i)
		d.save(flush: true, failOnError: true)

		then:
		PreviewImage.count() == 2 // one was created by createDocument
		Document.count() == 1
	}

	def "pageNumber is unique per document"() {
		given:
		assert PreviewImage.count() == 0
		assert Document.count() == 0

		when:
		def d = createDocument(authService: authService)
		d.addToPreviewImages(new PreviewImage(data: previewImageData, thumbnail: previewImageData, pageNumber:1, sourceHeight: 1))
		d.addToPreviewImages(new PreviewImage(data: previewImageData, thumbnail: previewImageData, pageNumber:1, sourceHeight: 2))
		d.save(flush: true, failOnError: true)

		// TODO this test is broken, saving with duplicate page numbers silently ignores the second page number
		then:
		notThrown()
		d.errors.allErrors.size() == 0
		PreviewImage.count() == 1
		Document.count() == 1
	}

	def "preview images are sorted by pageNumber"() {
		given:
		assert PreviewImage.count() == 0
		assert Document.count() == 0

		when:
		def d = createDocument(authService: authService)
		d.addToPreviewImages(new PreviewImage(data: previewImageData, thumbnail: previewImageData, pageNumber:2))
		d.addToPreviewImages(new PreviewImage(data: previewImageData, thumbnail: previewImageData, pageNumber:3))
		d.addToPreviewImages(new PreviewImage(data: previewImageData, thumbnail: previewImageData, pageNumber:1))
		d.save(flush: true, failOnError: true)

		then:
		PreviewImage.count() == 3
		Document.count() == 1
		Document.get(d.id).previewImages*.pageNumber == [1, 2, 3]
	}

	def "updates to previewimage cascade"() {
		given:
		assert PreviewImage.count() == 0
		assert Document.count() == 0

		when:
		def d = createDocument(authService: authService)
		d.addToPreviewImages(new PreviewImage(data: previewImageData, thumbnail: previewImageData, pageNumber:1, height: 1))
		d.save(flush: true, failOnError: true)
		d = Document.get(d.id)
		d.previewImage(1).sourceHeight = 2
		d.save(flush: true, failOnError: true)

		then:
		PreviewImage.count() == 1
		Document.count() == 1
		Document.get(d.id).previewImages*.sourceHeight == [2]
	}

	def "you cannot save a document without at least one file"() {
		given:
		assert DocumentData.count() == 2
		assert Document.count() == 0

		when:
		def d = createDocument(authService: authService)
		d.files.clear()
		def result = d.save(flush: true, failOnError: false)

		then:
		!result
		d.errors.getFieldError("files")
	}

	def "you cannot save a document without a group"() {
		given:
		assert DocumentData.count() == 2
		assert Document.count() == 0

		when:
		def d = createDocument(authService: authService)
		d.group = null
		def result = d.save(flush: true, failOnError: false)

		then:
		!result
		d.errors.getFieldError("group")
	}

	def "deleting a document does not cascade to files"() {
		given:
		assert DocumentData.count() == 2
		assert Document.count() == 0

		when:
		def d = createDocument(authService: authService)
		assert Document.count() == 1
		d.delete(flush: true)

		then:
		DocumentData.count() == 4 // 2 created by createDocument also
		Document.count() == 0
	}

	def "document data is sorted by dateCreated"() {
		given:
		assert DocumentData.count() == 2
		assert Document.count() == 0

		when: "the files are added"
		def d = createDocument(authService: authService)
		d.files.clear()
		d.addToFiles(previewImageData)
		d.save()
		d.addToFiles(fileData)
		d.save()

		then: "the file with the newest createDate should be first"
		fileData.dateCreated > previewImageData.dateCreated
		Document.get(d.id).files.first() == fileData
		DocumentData.count() == 4 // 2 created by createDocument also
		Document.count() == 1
	}

	def "document should store the createdBy on save"() {
		def d = createDocument(authService: authService)

		when:
		d.save()

		then:
		d.createdBy == user
		d.lastUpdatedBy == user
		d.dateCreated != null
		d.lastUpdated != null
	}

	def "document should store the lastUpdatedBy on update"() {
		def d = createDocument(authService: authService)

		when:
		d.save(flush: true)
		d = Document.get(d.id)
		def origLastUpdated = d.lastUpdated

		d.name = 'new name'
		d.lastUpdatedBy = null
		d.save(flush: true)

		then:
		d.createdBy == user
		d.lastUpdatedBy == user
		d.dateCreated != null
		d.lastUpdated != origLastUpdated
	}

	def "folder should store the createdBy on save"() {
		def f = getFolder()

		when:
		f.save(failOnError: true, flush: true)

		then:
		f.createdBy == user
		f.lastUpdatedBy == user
		f.dateCreated != null
		f.lastUpdated != null
	}

	def "folder should store the lastUpdatedBy on update"() {
		def f = getFolder()

		when:
		f.save(failOnError: true, flush: true)
		f = Folder.get(f.id)
		def origLastUpdated = f.lastUpdated

		f.name = 'new name'
		f.lastUpdatedBy = null
		f.save(fialOnError: true, flush: true)

		then:
		f.createdBy == user
		f.lastUpdatedBy == user
		f.dateCreated != null
		f.lastUpdated != origLastUpdated
	}
}
