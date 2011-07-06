package us.paperlesstech

import grails.plugin.spock.*
import spock.lang.*

class DocumentDataIntegrationSpec extends IntegrationSpec {
	def "immutability test"() {
		given:
			def dd = new DocumentData(fileKey: "fileKey", fileSize: 42, mimeType: MimeType.PNG)
			dd.save(flush:true)
		when:
			dd.fileSize = 24
			dd.save(flush:true)
		then:
			thrown(UnsupportedOperationException)
	}
}
