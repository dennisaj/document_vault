package us.paperlesstech.handlers

import grails.plugin.spock.*
import spock.lang.*
import us.paperlesstech.Document
import us.paperlesstech.DocumentData

class DefaultImageHandlerServiceSpec extends UnitSpec {
	def service = new DefaultImageHandlerService()

	def "cursiveSign method requires signatureData"() {
		when:
			service.cursiveSign([document:new Document(), documentData:new DocumentData(), signatures:["1":null]])
		then:
			thrown(AssertionError)
	}
}
