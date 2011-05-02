package us.paperlesstech.helpers

import grails.plugin.spock.UnitSpec

class FileHelpersSpec extends UnitSpec {
	def "chopExtension should properly remove the file extension"() {
		expect:	"Should return the path to the file minus the extension"
		FileHelpers.chopExtension("/tmp/file.pdf", ".pdf")  == "/tmp/file"
	}
}
