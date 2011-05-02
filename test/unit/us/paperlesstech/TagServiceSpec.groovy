package us.paperlesstech

import grails.plugin.spock.*
import spock.lang.*

class TagServiceSpec extends UnitSpec {
	def tagService = new TagService()

    def "sanitize should remove trailing slashes and commas"() {
		expect:
			cleanString == tagService.sanitize(dirtyString)

		where:
			dirtyString << ["/slashTest////", ",,c,,omma,test,", "/slash/Test/2", null]
			cleanString << ["/slashTest", "commatest", "/slash/Test/2",""]
    }
}
