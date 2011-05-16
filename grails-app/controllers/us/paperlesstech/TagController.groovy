package us.paperlesstech

import grails.converters.JSON

import org.apache.shiro.SecurityUtils
import org.grails.taggable.Tag

class TagController {
	static navigation = [[action:'index', isVisible: {SecurityUtils.subject.isPermitted("tag:*")}, order:20, title:"Tags"]]

	def tagService

	def create = {
		if (tagService.createTag(params.name)) {
			render(template:"/saved", model:[message:"Your tag was saved"])
			return
		}

		render(template:"/notsaved", model:[message:"Your tag could not be saved"])
		return
	}

	def documentAdd = {
		if (tagService.addDocumentTag(params.id, params.tag)) {
			render(template:"/saved", model:[message:"Document(${params.id}) was tagged as '${params.tag}'"])
			return
		}

		render(template:"/notsaved", model:[message:"Document(${params.id}) could not be tagged"])
		return
	}

	def documentList = {
		// IE caches GET requests. These lines prevent that
		response.setHeader('Last-Modified', '${now}')
		response.setHeader('Cache-Control', 'no-store, no-cache, must-revalidate, post-check=0, pre-check=0')
		response.setHeader('Pragma', 'No-cache')

		render (tagService.getDocumentTags(params.id) as JSON)
	}

	def documentRemove = {
		def tag = params.tag?.trim()

		render ([status:tagService.removeDocumentTag(params.id?.trim(), tag) ? "success" : "error"] as JSON)
	}

	def documents = {
		def name = params.name?.trim()
		if (name) {
			render(template:"allTagged", model:[documents:Document.findAllByTag(name), tag:name])
		} else {
			render(template:"untagged", model:[untagged:tagService.untaggedDocuments()])
		}
	}

	def index = {
		[untagged:tagService.untaggedDocuments(), tagSearchResults:tagService.getRecentTags()]
	}

	def list = {
		def term = params.term?.trim()
		if (term) {
			render (Tag.findAllByNameIlike("${term}%", [max:20, sort:"name", order:"asc"])*.name as JSON)
		} else {
			render ([:] as JSON)
		}
	}

	def search = {
		def results = []
		def query = params.q?.trim()
		def terms = query?.tokenize(",").collect { it.trim() }.findAll { it }
		if (terms?.size()) {
			results = tagService.tagSearch(terms)
		} else {
			results = tagService.getRecentTags()
		}

		render(template:"tagSearchResults", model:[tagSearchResults:results])
	}
}
