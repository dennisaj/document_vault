package us.paperlesstech

import grails.plugin.spock.UnitSpec

class SignatureServiceSpec extends UnitSpec {
	def service = new SignatureService()
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

	def "save signatures to map should convert a string to a byte[] and add it to the map"() {
		when: "Save signature is called"
		service.saveSignatureToMap signatures, pageNumber, imageData

		then: "Then it should populate the map with the encoded string decoded into a byte[]"
		signatures.size() == 1
		signatures[pageNumber] == rawBytes

		where:
		signatures = [5: new byte[0]]
		pageNumber = 5
	}
}
