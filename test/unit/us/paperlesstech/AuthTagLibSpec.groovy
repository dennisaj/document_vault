package us.paperlesstech

import grails.plugin.spock.TagLibSpec

import org.codehaus.groovy.grails.web.pages.GroovyPage
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException

import spock.lang.Shared
import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.User

class AuthTagLibSpec extends TagLibSpec {
	@Shared
	def methods

	def setup() {
		tagLib.authServiceProxy = Mock(AuthService)
		tagLib.authServiceProxy./can.*/(_) >> true
		tagLib.authServiceProxy./can.*Any/() >> true

		methods = tagLib.properties.keySet().findAll{key->
			key ==~ /can.*/
		}
	}

	def "tags that require a field should throw error when it is missing"() {
		when:
			"$method"()
		then:
			thrown(GrailsTagException)
		where:
			method << ["canDelete", "canGetSigned", "canNotes", "canPrint", "canSign", "canTag", "canUpload", "canView", "canRunAs"]
	}

	def "empty body should return boolean"() {
		when:
			def ret = "$method"(user:new User(), document:new Document(), group: new Group(), GroovyPage.EMPTY_BODY_CLOSURE)
		then:
			ret in ["true", "false"]
		where:
			method << methods
	}

	def "non-empty body should return result of closure"() {
		when:
			def ret = "$method"(user:new User(), document:new Document(), group: new Group()) {
				"output"
			}
		then:
			ret == "output"
		where:
			method << methods
	}

	def "null should be output when permissions is denied"() {
		given:
			tagLib.authServiceProxy = Mock(AuthService)
			tagLib.authServiceProxy./can.*/(_) >> false
			tagLib.authServiceProxy./can.*Any/() >> false
		when:
			def ret = "$method"(user:new User(), document:new Document(), group: new Group()) {
				"output"
			}
		then:
			ret == "null"
		where:
			method << methods
	}
}

