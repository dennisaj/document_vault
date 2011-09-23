package us.paperlesstech

import grails.plugin.spock.UnitSpec
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.grails.taggable.Tag

@Mixin(MetaClassMixin)
class TagServiceSpec extends UnitSpec {
	def tagService = new TagService()

	def setup() {
		if (!ConfigurationHolder.config) {
			ConfigurationHolder.config = new ConfigObject()
		}
		//		ConfigurationHolder.config.grails.taggable.preserve.case == true
	}

	def cleanup() {
		Document.metaClass = null
	}

	def "sanitize should remove trailing slashes and commas"() {
		expect:
		cleanString == tagService.sanitize(dirtyString)

		where:
		dirtyString << ["/slashTest////", ",,c,,omma,test,", "/slash/Test/2", null]
		cleanString << ["/slashTest", "commatest", "/slash/Test/2", ""]
	}

	def getDocument(nullDocument = false) {
		if (nullDocument) {
			return null
		} else {
			return new Document()
		}
	}

	def "addDocumentTag adds a tag to the document if both exist"() {
		given:
		Document.metaClass.addTag = {String tag -> tag}

		expect:
		tagService.addDocumentTag(document, tag) == result

		where:
		document          | tag   | result
		getDocument(true) | null  | false
		getDocument()     | null  | false
		null              | "tag" | false
		getDocument()     | "tag" | true
	}

	def "createTag should return a tag with errors"() {
		given:
		mockDomain(Tag)
		mockForConstraintsTests(Tag)

		expect:
		tagService.createTag("").hasErrors()
	}

	def "createTag should a saved tag if no errors"() {
		given:
		mockDomain(Tag)
		mockForConstraintsTests(Tag)

		when:
		def tag = tagService.createTag("tagTest")

		then:
		!tag.hasErrors()
		tag.id
	}

	def "getDocumentTags returns nothing if the document does not exist"() {
		given:
		mockDomain(Document)

		expect:
		tagService.getDocumentTags(-1L) == []
	}

	def "getDocumentTags returns an empty list if the document has not tags"() {
		given:
		def d = new Document()
		d.id = 42
		Document.metaClass.getTags = {
			[]
		}
		mockDomain(Document, [d])

		expect:
		tagService.getDocumentTags(42L) == []
	}

	def "getDocumentTags returns escaped tag names"() {
		given:
		def d = new Document()
		d.id = 42
		Document.metaClass.getTags = {
			["normal", "&", "<"]
		}
		mockDomain(Document, [d])

		expect:
		tagService.getDocumentTags(42L) == ["normal", "&amp;", "&lt;"]
	}

	def "getRecentTags returns an empty list if no tags"() {
		given:
		mockDomain(Tag)

		expect:
		tagService.getRecentTags() == []
	}

	def "getRecentTags returns 5 most recent tags"() {
		given:
		def tags = (1..10).collect {
			def tag = new Tag(name: "tag$it")
			tag.id = it
			tag
		}
		mockDomain(Tag, tags)

		expect:
		tagService.getRecentTags() == ["tag10", "tag9", "tag8", "tag7", "tag6"]
	}

	def "removeDocumentTag does nothing if the document does not exist"() {
		tagService.removeDocumentTag(null, null) == false
	}

	def "removeDocumentTag tries to remove the tag if the document exists"() {
		def d = new Document()
		Document.metaClass.removeTag = { String tagName ->
			assert tagName == "testTag"
		}

		tagService.removeDocumentTag(d, "testTag") == true
	}
}
