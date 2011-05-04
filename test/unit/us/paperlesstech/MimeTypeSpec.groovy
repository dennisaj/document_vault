package us.paperlesstech

import grails.plugin.spock.UnitSpec

class MimeTypeSpec extends UnitSpec {
	def "import from mimeType"() {
		expect:
		MimeType.getMimeType(mimeType:"iMaGe/pNg") == MimeType.PNG
	}

	def "import from extension"() {
		expect:
		MimeType.getMimeType(fileName:"test.pDf") == MimeType.PDF
	}

	def "mimeType unknown"() {
		expect:
		MimeType.getMimeType() == null
		MimeType.getMimeType(mimeType:"asdf") == null
		MimeType.getMimeType(fileName:"pdf") == null
		MimeType.getMimeType(fileName:"asdf.asdf") == null
	}
}
