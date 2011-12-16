package us.paperlesstech

import spock.lang.Specification

class DocumentDataSpec extends Specification {
	def "test the constraints"() {
		given:
		mockForConstraintsTests(DocumentData)

		when:
		def dd = new DocumentData()
		def result = dd.validate()

		then:
		result == false
		dd.errors.hasFieldErrors("fileKey")
		dd.errors.hasFieldErrors("fileSize")
		dd.errors.hasFieldErrors("mimeType")
	}
}
