package us.paperlesstech

import grails.plugin.spock.UnitSpec

import grails.plugin.spock.UnitSpec;

class DocumentServiceSpec extends UnitSpec {
	DocumentService service = new DocumentService()
	byte[] rawBytes = new byte[8];
	String encodedBytes = null
	String imageData = null

	def setup() {
		"CAFEBABE".eachWithIndex { it, idx ->
			rawBytes[idx] = Byte.decode("#$it")
		}
		encodedBytes = rawBytes.encodeBase64().toString()
		imageData = "data:image/png;base64," + encodedBytes
	}

	def "chopExtension should properly remove the file extension"() {
		expect:	"Should return the path to the file minus the extension"
		service.chopExtension("/tmp/file.pdf", ".pdf")  == "/tmp/file"
	}

	def "save signatures to map should convert a string to a byte[] and add it to the map"() {
		when:"Save signature is called"
		service.saveSignatureToMap signatures, pageNumber, imageData

		then:"Then it should populate the map with the encoded string decoded into a byte[]"
		signatures.size() == 1
		signatures[pageNumber] == rawBytes

		where:
		signatures = [5: new byte[0]]
		pageNumber = 5
	}

	def "test getting image data as a map"() {
		given: "A mocked domain"
		def doc = new Document(id:documentId)
		mockDomain(Document, [doc])
		doc.addToImages(new Image(data: rawBytes,
				pageNumber: pageNumber,
				sourceHeight: sourceHeight,
				sourceWidth: sourceWidth))

		when: "Map is called"
		def m = service.getImageDataAsMap(documentId, pageNumber)

		then: "It should encode the image as base64 and return the other fields"
		m.sourceWidth == sourceWidth
		m.sourceHeight == sourceHeight
		m.imageData == imageData
		m.pageNumber == pageNumber

		where:
		documentId = 1
		pageNumber = 0
		sourceWidth = 800
		sourceHeight = 600
	}
}