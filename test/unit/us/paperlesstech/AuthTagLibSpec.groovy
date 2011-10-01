
package us.paperlesstech

import grails.plugin.spock.TagLibSpec

import org.codehaus.groovy.grails.web.pages.GroovyPage
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException

import spock.lang.Shared
import spock.lang.Unroll
import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.User

class AuthTagLibSpec extends TagLibSpec {
	@Shared
	def methods

	def setup() {
		tagLib.authService = Mock(AuthService)
		tagLib.authService./can.*/(_) >> true
		tagLib.authService./can.*Any(Document|Group)?/() >> true

		methods = tagLib.properties.keySet().findAll{ key->
			key ==~ /can.*/
		}
	}

	@Unroll("Testing if the method #method throws an error when a field is missing")
	def "tags that require a field should throw error when it is missing"() {
		when:
			"$method"()
		then:
			thrown(GrailsTagException)
		where:
			method << ["canDelete", "canGetSigned", "canNotes", "canPrint", "canSign", "canUpload", "canView", "canRunAs"]
	}

	@Unroll("Testing if the method #method returns a boolean when given an empty body")
	def "empty body should return boolean"() {
		when:
			def ret = "$method"(user:new User(), document:new Document(), group:new Group(), GroovyPage.EMPTY_BODY_CLOSURE)
		then:
			ret in ["true", "false"]
		where:
			method << methods
	}

	@Unroll("Testing if the method #method returns a boolean when given a non-empty body")
	def "non-empty body should return result of closure"() {
		when:
			def ret = "$method"(user:new User(), document:new Document(), group:new Group()) {
				"output"
			}
		then:
			ret == "output"
		where:
			method << methods
	}

	@Unroll("Testing if the method #method returns null when permission is denied")
	def "null should be output when permissions is denied"() {
		given:
			tagLib.authService = Mock(AuthService)
			tagLib.authService./can.*/(_) >> false
			tagLib.authService./can.*Any(Document|Group)?/() >> false
		when:
			def ret = "$method"(user:new User(), document:new Document(), group:new Group()) {
				"output"
			}
		then:
			ret == "null"
		where:
			method << methods
	}
}

