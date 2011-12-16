package us.paperlesstech

import spock.lang.Unroll
import spock.lang.Specification

class PreviewImageSpec extends Specification {
	@Unroll({"testConstraints new PreviewImage($pageNumber, $data, $thumbnail).validate() ==  $expected"})
	def "test constraints"() {
		given:
		mockForConstraintsTests(PreviewImage)

		when:
		def i = new PreviewImage(pageNumber: pageNumber, data: data, thumbnail: thumbnail)
		i.document = new Document()
		def result = i.validate()

		then:
		result == expected

		where:
		pageNumber | data           | thumbnail      | expected
		null       | null           | null           | false
		1          | null           | null           | false
		null       | documentData() | null           | false
		1          | documentData() | null           | false
		null       | null           | documentData() | false
		1          | null           | documentData() | false
		null       | documentData() | documentData() | false
		1          | documentData() | documentData() | true
	}

	def documentData() {
		return new DocumentData(mimeType: MimeType.PDF, fileSize: 2, fileKey: "key")
	}
}
