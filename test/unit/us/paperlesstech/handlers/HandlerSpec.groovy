package us.paperlesstech.handlers

import grails.plugin.spock.UnitSpec
import us.paperlesstech.AuthService
import us.paperlesstech.Document
import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType
import us.paperlesstech.PreviewImage

class HandlerSpec extends UnitSpec {
	def authService = Mock(AuthService)
	def handler = new Handler()

	def setup() {
		handler.authService = authService
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

	def "download should return a triple of the file data"() {
		given:
		def data = new byte[1]
		def d = new Document(name: "file")
		authService.canView(d) >> true
		def dd = new DocumentData(mimeType: mimeType, data: data)

		when:
		def result = handler.download(document: d, documentData: dd)

		then:
		result[0] == "file.pdf"
		result[1] == data
		result[2] == mimeType.downloadContentType

		where:
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

	def "downloadPreview should return a triple of the preview file data"() {
		given:
		mockDomain(Document)
		def data = new byte[1]
		def d = new Document(name: "file")
		authService.canView(d) >> true
		def dd = new DocumentData(mimeType: mimeType, data: data)
		def pi = new PreviewImage(pageNumber: pageNumber, data: dd)
		d.addToPreviewImages(pi)

		when:
		def result = handler.downloadPreview(document: d, page: pageNumber)

		then:
		result[0] == "file - page($pageNumber).png"
		result[1] == data
		result[2] == mimeType.downloadContentType

		where:
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
}
