package us.paperlesstech

import grails.plugin.spock.UnitSpec

class PreviewImageSpec extends UnitSpec {
	def "test constraints"() {
		given:
		mockForConstraintsTests(PreviewImage)

   		when:
		def i = new PreviewImage(pageNumber: pageNumber, data: data)
		i.document = new Document()
		def result = i.validate()

		then:
		result == expected

		where:
		pageNumber	| data					| expected
		null		| null					| false
		1 			| null					| false
		null 		| documentData()		| false
		1 			| documentData()		| true
	}

	def documentData() {
		return new DocumentData(mimeType:MimeType.PDF, data:new byte[1])
	}
}
