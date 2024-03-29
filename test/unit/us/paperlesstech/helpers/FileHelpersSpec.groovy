package us.paperlesstech.helpers

import grails.plugin.spock.UnitSpec

class FileHelpersSpec extends UnitSpec {
	def "chopExtension should properly remove the file extension"() {
		expect:	"Should return the path to the file minus the extension"
		FileHelpers.chopExtension("/tmp/file.pdf", ".pdf")  == "/tmp/file"
	}

	def "chopExtension should properly remove the file extension even when it is not supplied"() {
		expect:	"Should return the path to the file minus the extension"
		FileHelpers.chopExtension("/tmp/file.pdf")  == "/tmp/file"

		FileHelpers.chopExtension("/tmp/file")  == "/tmp/file"
	}

	def "getExtension should return the file extension"() {
		expect:
		FileHelpers.getExtension("/tmp/file.pDf.png") == ".png"
		!FileHelpers.getExtension(".")
		!FileHelpers.getExtension("")
		!FileHelpers.getExtension(null)
	}
}
