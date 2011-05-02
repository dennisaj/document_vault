package us.paperlesstech

import grails.plugin.spock.UnitSpec
import static us.paperlesstech.MimeType.PDF

class DocumentDataSpec extends UnitSpec {
    def "test the constraints"() {
		given:
		mockForConstraintsTests(DocumentData)

		when:
		def bd = new DocumentData(mimeType: mimeType, data: data)
		def result = bd.validate()

		then:
		result == expected

		where:
		mimeType	| data			| expected
		null		| null			| false
		PDF 		| null			| false
		null 		| new byte[1]	| false
		PDF 		| new byte[1]	| true
    }
}
