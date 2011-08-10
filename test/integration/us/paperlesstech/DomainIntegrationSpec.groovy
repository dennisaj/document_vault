package us.paperlesstech

import grails.plugin.spock.IntegrationSpec
import us.paperlesstech.nimble.Group

class DomainIntegrationSpec extends IntegrationSpec {
	def sessionFactory
	def previewImageData
	def fileData
	def group
	def firstDateCreated = new GregorianCalendar(2011, 1, 1).time
	def secondDateCreated = new GregorianCalendar(2010, 1, 1).time

	def setup() {
		previewImageData = new DocumentData(mimeType: MimeType.PDF, fileKey: "previewImageDataKey", fileSize: 1,
				dateCreated: firstDateCreated)
		previewImageData.save()
		fileData = new DocumentData(mimeType: MimeType.PDF, fileKey: "fileDataKey", fileSize: 1,
				dateCreated: secondDateCreated)
		fileData.save()
		group = getGroup()
	}

	static def getGroup() {
		def g = new Group(name: "test " + Math.random())
		g.save()
		g
	}

	def getDocument() {
		def d = new Document()
		d.addToFiles(fileData)
		d.group = group
		d
	}

	def "can add to document maps"() {
		given:
		assert Document.count() == 0
		assert DocumentSearchField.count() == 0
		assert DocumentOtherField.count() == 0

		when:
		def d = getDocument()
		d.searchField("field1", "value1")
		d.otherField("field1","value2")
		def result = d.save(failOnError: true)
		d = Document.get(1)

		then: "Adding to the document map it should not throw a NPE"
		result
		Document.count() == 1
		DocumentSearchField.count() == 1
		DocumentOtherField.count() == 1
		d.searchField("field1") == "value1"
		d.otherField("field1") == "value2"
	}

	def "can update document maps"() {
		given:
		assert Document.count() == 0
		assert DocumentSearchField.count() == 0
		assert DocumentOtherField.count() == 0
		def d = getDocument()
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
		def d = getDocument()
		d.save()

		then:
		PreviewImage.count() == 0
		Document.count() == 1
	}

	def "saving a document cascades to a preview image"() {
		given:
		assert PreviewImage.count() == 0
		assert Document.count() == 0

		when:
		def d = getDocument()
		d.save()
		def i = new PreviewImage(data: previewImageData, thumbnail: previewImageData, pageNumber:1)
		d.addToPreviewImages(i)
		d.save()

		then:
		PreviewImage.count() == 1
		Document.count() == 1
	}

	def "pageNumber is unique per document"() {
		given:
		assert PreviewImage.count() == 0
		assert Document.count() == 0

		when:
		def d = getDocument()
		d.addToPreviewImages(new PreviewImage(data: previewImageData, thumbnail: previewImageData, pageNumber:1, sourceHeight: 1))
		d.addToPreviewImages(new PreviewImage(data: previewImageData, thumbnail: previewImageData, pageNumber:1, sourceHeight: 2))
		d.save(failOnError: true)

		// TODO this test is broken, saving with duplicate page numbers silently ignores the second page number
		then:
		notThrown()
		d.errors.allErrors.size() == 0
		PreviewImage.count() == 1
		Document.count() == 1
		Document.get(d.id).previewImages*.sourceHeight == [1]
	}

	def "preview images are sorted by pageNumber"() {
		given:
		assert PreviewImage.count() == 0
		assert Document.count() == 0

		when:
		def d = getDocument()
		d.addToPreviewImages(new PreviewImage(data: previewImageData, thumbnail: previewImageData, pageNumber:2))
		d.addToPreviewImages(new PreviewImage(data: previewImageData, thumbnail: previewImageData, pageNumber:3))
		d.addToPreviewImages(new PreviewImage(data: previewImageData, thumbnail: previewImageData, pageNumber:1))
		d.save()

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
		def d = getDocument()
		d.addToPreviewImages(new PreviewImage(data: previewImageData, thumbnail: previewImageData, pageNumber:1, height: 1))
		d.save()
		d = Document.get(d.id)
		d.previewImage(1).sourceHeight = 2
		d.save()

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
		def d = getDocument()
		d.files.clear()
		def result = d.save()

		then:
		!result
		d.errors.getFieldError("files")
		DocumentData.count() == 2
		Document.count() == 0
	}

	def "you cannot save a document without a group"() {
		given:
		assert DocumentData.count() == 2
		assert Document.count() == 0

		when:
		def d = getDocument()
		d.group = null
		def result = d.save()

		then:
		!result
		d.errors.getFieldError("group")
		DocumentData.count() == 2
		Document.count() == 0
	}

	def "deleting a document does not cascade to files"() {
		given:
		assert DocumentData.count() == 2
		assert Document.count() == 0

		when:
		def d = getDocument()
		d.save(failOnError:true)
		assert Document.count() == 1
		d.delete()

		then:
		DocumentData.count() == 2
		Document.count() == 0
	}

	def "document data is sorted by dateCreated"() {
		given:
		assert DocumentData.count() == 2
		assert Document.count() == 0

		when: "the files are added"
		def d = getDocument()
		d.files.clear()
		d.addToFiles(previewImageData)
		d.save()
		d.addToFiles(fileData)
		d.save()

		then: "the file with the newest createDate should be first"
		fileData.dateCreated > previewImageData.dateCreated
		Document.get(d.id).files.first() == fileData
		DocumentData.count() == 2
		Document.count() == 1
	}
}
