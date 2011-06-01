package us.paperlesstech

import grails.plugin.spock.UnitSpec
import grails.plugins.nimble.core.Group
import spock.lang.Unroll

class DocumentSpec extends UnitSpec {
	byte[] rawBytes = new byte[8];
	String encodedBytes = null

	def setup() {
		"CAFEBABE".eachWithIndex { it, idx ->
			rawBytes[idx] = Byte.decode("#$it")
		}
		encodedBytes = rawBytes.encodeBase64().toString()

	}

	@Unroll("saving document with file=#file and group=#group")
	def "test constraints"() {
		given:
		mockDomain(Document)
		mockForConstraintsTests(Document)

		when:
		def d = new Document()
		d.group = group
		if (file)
			d.addToFiles(file)
		def result = d.validate()

		then:
		result == expected

		where:
		file           | group       | expected
		null           | null        | false
		documentData() | null        | false
		null           | new Group() | false
		documentData() | new Group() | true
	}

	def documentData() {
		new DocumentData(mimeType: MimeType.PDF, data: new byte[1])
	}


	def "test getting image data as a map"() {
		given: "A document with images"
		mockDomain(Document)
		def doc = new Document()
		DocumentData dd = new DocumentData(data: rawBytes)
		doc.addToPreviewImages(new PreviewImage(data: dd,
				pageNumber: pageNumber,
				height: height,
				width: width))

		when: "Map is called"
		def m = doc.previewImageAsMap(pageNumber)

		then: "It should return the width, height and pageNumber"
		m.sourceWidth == width
		m.sourceHeight == height
		m.pageNumber == pageNumber

		where:
		pageNumber = 5
		width = 800
		height = 600
	}

	def "test loading previewImage by pageNumber"() {
		given: "A document with images"
		mockDomain(Document)
		def doc = new Document()
		DocumentData dd = new DocumentData(data: rawBytes)
		doc.addToPreviewImages(new PreviewImage(data: dd,
				pageNumber: pageNumber,
				height: height,
				width: width))

		expect:
		doc.previewImage(5).pageNumber == pageNumber
		doc.previewImage(0) == null

		where:
		pageNumber = 5
		width = 800
		height = 600

	}
}