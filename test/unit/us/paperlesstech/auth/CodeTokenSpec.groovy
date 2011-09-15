package us.paperlesstech.auth

import grails.plugin.spock.UnitSpec

class CodeTokenSpec extends UnitSpec {
	def "getCredentials should return the code"() {
		given:
		def token = new CodeToken()
		token.code = code

		expect:
		token.credentials == code

		where:
		code = "1234"
	}

	def "getPrincipal should return the code"() {
		given:
		def token = new CodeToken()
		token.code = code

		expect:
		token.principal == code

		where:
		code = "1234"
	}
}
