package us.paperlesstech

import grails.plugin.spock.*
import spock.lang.*

class DocumentDataIntegrationSpec extends IntegrationSpec {
	def "immutability test"() {
		given:
			def dd = new DocumentData(data:[1], mimeType: MimeType.PNG)
			dd.save(flush:true)
		when:
			dd.data = [2]
			dd.save(flush:true)
		then:
			thrown(UnsupportedOperationException)
	}
}
