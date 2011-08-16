package us.paperlesstech.handlers

import grails.plugin.spock.UnitSpec
import us.paperlesstech.AuthService
import us.paperlesstech.Document
import us.paperlesstech.DocumentData
import us.paperlesstech.PreferenceService
import us.paperlesstech.Printer
import us.paperlesstech.nimble.Group

class HandlerChainSpec extends UnitSpec {
	HandlerChain chain
	AuthService authService = Mock()
	PreferenceService preferenceService = Mock()
	def line = [a:[x:0,y:0], b:[x:100,y:100]]

	def setup() {
		chain = new HandlerChain()
		chain.authServiceProxy = authService
		chain.preferenceService = preferenceService
	}

	def "importFile fails if the user can't upload to the document group"() {
		given:
		def d = new Document()
		d.group = new Group()

		when:
		chain.importFile(document: d)

		then:
		thrown AssertionError
		1 * authService.canUpload(d.group) >> false
	}

	def "importFile calls handle"() {
		given:
		def d = new Document()
		d.group = new Group()
		def bytes = new byte[1]
		def m = [document: d, bytes: bytes]
		def methodName
		def input
		chain.metaClass.handle = { String a, Map b -> methodName = a; input = b }

		when:
		chain.importFile(document: d, bytes: bytes)

		then:
		1 * authService.canUpload(d.group) >> true
		1 * preferenceService.setPreference(_, _, _) >> true
		methodName == "importFile"
		input == m
	}

	def "importFile fails when there is no byte array"() {
		given:
		def d = new Document()

		when:
		chain.importFile(document: d)

		then:
		thrown AssertionError
		1 * authService.canUpload(d.group) >> true
	}

	def "generatePreview fails if the user can't upload to the document group and can't sign the document"() {
		given:
		def d = new Document()
		d.group = new Group()

		when:
		chain.generatePreview(document: d)

		then:
		thrown AssertionError
		1 * authService.canUpload(d.group) >> false
		1 * authService.canSign(d) >> false
	}

	def "generatePreview calls handle"() {
		given:
		def d = new Document()
		d.group = new Group()
		def m = [document: d]
		def methodName
		def input
		chain.metaClass.handle = { String a, Map b -> methodName = a; input = b }

		when:
		chain.generatePreview(document: d)

		then:
		1 * authService.canUpload(d.group) >> false
		1 * authService.canSign(d) >> true
		methodName == "generatePreview"
		input == m
	}

	def "print fails if the user can't print the document"() {
		given:
		def d = new Document()

		when:
		chain.print(document: d, printer:new Printer(id:1))

		then:
		thrown AssertionError
		1 * authService.canPrint(d) >> false
	}

	def "print fails if there is no printer"() {
		given:
		def d = new Document()

		when:
		chain.print(document: d)

		then:
		thrown AssertionError
	}

	def "print calls handle"() {
		given:
		def d = new Document()
		def p = new Printer(id:1)
		def m = [document: d, printer:p]
		def methodName
		def input
		chain.metaClass.handle = { String a, Map b -> methodName = a; input = b }

		when:
		chain.print(document: d,, printer:p)

		then:
		1 * authService.canPrint(d) >> true
		methodName == "print"
		input == m
	}

	def "cursiveSign fails if the user can't sign the document"() {
		given:
		def d = new Document()

		when:
		chain.cursiveSign(document: d)

		then:
		thrown AssertionError
		1 * authService.canSign(d) >> false
	}

	def "sign calls handle"() {
		given:
		def d = new Document()
		def m = [document: d]
		def methodName
		def input
		chain.metaClass.handle = { String a, Map b -> methodName = a; input = b }

		when:
		chain.cursiveSign(document: d)

		then:
		1 * authService.canSign(d) >> true
		methodName == "cursiveSign"
		input == m
	}

	def "downloadPreview fails if the user can't view the document"() {
		given:
		def d = new Document()

		when:
		chain.downloadPreview(document: d)

		then:
		thrown AssertionError
		1 * authService.canView(d) >> false
	}

	def "downloadPreview calls handle"() {
		given:
		def d = new Document()
		def m = [document: d]
		def methodName
		def input
		chain.metaClass.handle = { String a, Map b -> methodName = a; input = b }

		when:
		chain.downloadPreview(document: d)

		then:
		1 * authService.canView(d) >> true
		methodName == "downloadPreview"
		input == m
	}

	def "download fails if the user can't view the document"() {
		given:
		def d = new Document()

		when:
		chain.download(document: d)

		then:
		thrown AssertionError
		1 * authService.canView(d) >> false
	}

	def "download calls handle"() {
		given:
		def d = new Document()
		def m = [document: d]
		def methodName
		def input
		chain.metaClass.handle = { String a, Map b -> methodName = a; input = b }

		when:
		chain.download(document: d)

		then:
		1 * authService.canView(d) >> true
		methodName == "download"
		input == m
	}

	def "downloadNote fails if the user can't notes the document"() {
		given:
		def d = new Document()

		when:
		chain.downloadNote(document: d, documentNote:new DocumentData())

		then:
		thrown AssertionError
		1 * authService.canNotes(d) >> false
	}

	def "saveNotes fails if the user can't notes the document"() {
		given:
		def d = new Document()

		when:
		chain.saveNotes(document: d, notes: [1:[line]])

		then:
		thrown AssertionError
		1 * authService.canNotes(d) >> false
	}
}
