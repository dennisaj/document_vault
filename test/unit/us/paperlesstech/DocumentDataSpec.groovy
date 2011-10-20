package us.paperlesstech

import grails.plugin.spock.UnitSpec

class DocumentDataSpec extends UnitSpec {
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
