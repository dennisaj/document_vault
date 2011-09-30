package us.paperlesstech.handlers

import grails.plugin.spock.UnitSpec
import us.paperlesstech.AuthService
import us.paperlesstech.Document
import us.paperlesstech.DocumentData
import us.paperlesstech.FileService
import us.paperlesstech.MimeType
import us.paperlesstech.Note
import us.paperlesstech.PreviewImage

class HandlerSpec extends UnitSpec {
	def authService = Mock(AuthService)
	def handler = new Handler()
	FileService fileService = Mock()
	def line = [a:[x:0,y:0], b:[x:100,y:100]]

	def setup() {
		handler.authService = authService
		handler.fileService = fileService
	}

	def "expected to be overloaded methods should throw an exception"() {
		when: "This should throw an exception"
		handler."$method"([:])

		then:
		thrown UnsupportedOperationException

		where:
		method << ["importFile", "generatePreview", "print", "cursiveSign"]
	}

	def "getDocument returns the document from the map"() {
		given:
		def doc = new Document()

		expect:
		doc == handler.getDocument(document: doc)
	}

	def "getDocument throws an exception when there is no document"() {
		when:
		handler.getDocument()

		then:
		thrown AssertionError
	}

	def "getDocument throws an exception when document is the wrong type"() {
		when:
		handler.getDocument(document: 5)

		then:
		thrown AssertionError
	}

	def "getDocumentData returns the documentData from the map"() {
		given:
		def dd = new DocumentData()

		expect:
		dd == handler.getDocumentData(documentData: dd)
	}

	def "getDocumentData throws an exception when there is no documentData"() {
		when:
		handler.getDocumentData()

		then:
		thrown AssertionError
	}

	def "getDocumentData throws an exception when documentData is the wrong type"() {
		when:
		handler.getDocumentData(documentData: 5)

		then:
		thrown AssertionError
	}

	def "setDocument sets the document in the map"() {
		given:
		def d = new Document()
		def m = [:]

		when:
		handler.setDocument(m, d)

		then:
		d == m.document
	}

	def "setDocument fails when map or document is null"() {
		when:
		handler.setDocument(m, d)

		then:
		thrown AssertionError


		where:
		d              | m
		null           | null
		null           | [:]
		new Document() | null
	}

	def "setDocumentData sets the document in the map"() {
		given:
		def d = new DocumentData()
		def m = [:]

		when:
		handler.setDocumentData(m, d)

		then:
		d == m.documentData
	}

	def "setDocumentData fails when map or document is null"() {
		when:
		handler.setDocumentData(m, d)

		then:
		thrown AssertionError


		where:
		d                  | m
		null               | null
		null               | [:]
		new DocumentData() | null
	}

	def "download should return a quad of the file data"() {
		given:
		def d = new Document(name: "file")
		authService.canView(d) >> true
		def dd = new DocumentData(mimeType: mimeType, fileSize: fileSize)
		def is = Mock(InputStream)

		when:
		def result = handler.download(document: d, documentData: dd)

		then:
		1 * fileService.getInputStream(dd) >> is
		result[0] == "file.pdf"
		result[1] == is
		result[2] == mimeType.downloadContentType
		result[3] == fileSize

		where:
		fileSize = 42
		mimeType = MimeType.PDF
	}

	def "download should throw an exception if the user can't view the document"() {
		given:
		def d = new Document()
		authService.canView(d) >> false
		def dd = new DocumentData()

		when:
		def result = handler.download(document: d, documentData: dd)

		then:
		thrown AssertionError
	}

	def "downloadPreview should return a quad of the preview file data"() {
		given:
		mockDomain(Document)
		def d = new Document(name: "file")
		authService.canView(d) >> true
		def is = Mock(InputStream)
		def dd = new DocumentData(mimeType: mimeType, fileSize: fileSize)
		def pi = new PreviewImage(pageNumber: pageNumber, data: dd)
		d.addToPreviewImages(pi)

		when:
		def result = handler.downloadPreview(document: d, page: pageNumber)

		then:
		1 * fileService.getInputStream(dd) >> is
		result[0] == "file-page($pageNumber).png"
		result[1] == is
		result[2] == mimeType.downloadContentType
		result[3] == fileSize

		where:
		fileSize = 42
		pageNumber = 2
		mimeType = MimeType.PNG
	}

	def "downloadPreview should throw an exception if the user can't view the document"() {
		given:
		def d = new Document()
		authService.canView(d) >> false
		def dd = new DocumentData()

		when:
		def result = handler.download(document: d, page: 1)

		then:
		thrown AssertionError
	}

	def "downloadNote should throw an exception if the user doesn't have notes permission for the document"() {
		given:
		def d = new Document()
		authService.canNotes(d) >> false
		def dd = new DocumentData()

		when:
		def result = handler.downloadNote(document: d)

		then:
		thrown AssertionError
	}

	def "saveNotes should throw an exception if the user doesn't have notes permission for the document"() {
		given:
		def d = new Document()
		authService.canNotes(d) >> false
		def dd = new DocumentData()

		when:
		def result = handler.saveNotes(document: d, notes: [1:[line]])

		then:
		thrown AssertionError
	}

	def "downloadNote should return a quad of the document note"() {
		given:
		mockDomain(Document)
		mockDomain(Note)
		def d = new Document(name:"file")
		authService.canNotes(d) >> true
		def is = Mock(InputStream)
		def note = new Note(id:1, data:new DocumentData(id:1,mimeType: mimeType, fileSize: fileSize))
		d.addToNotes(note)

		when:
		def result = handler.downloadNote(document: d, note:note)

		then:
		1 * fileService.getInputStream(note.data) >> is
		result[0] == "file-note($note.id)-${note.data.id}.$mimeType.downloadExtension"
		result[1] == is
		result[2] == mimeType.downloadContentType
		result[3] == fileSize

		where:
		fileSize = 42
		pageNumber = 2
		mimeType = MimeType.PNG
	}
}
