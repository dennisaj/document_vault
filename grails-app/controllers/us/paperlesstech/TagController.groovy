package us.paperlesstech

import grails.converters.JSON

import org.grails.taggable.Tag

class TagController {
	static navigation = [[action:'index', isVisible: {authService.canTagAny()}, order:20, title:"Tags"]]

	def authService
	def tagService

	def create = {
		if (!tagService.createTag(params.name).hasErrors()) {
			render(template:"/saved", model:[body:{g.message(code:"document-vault.tag.saved")}])
			return
		}

		render(template:"/notsaved", model:[message:g.message(code:"document-vault.tag.notsaved")])
		return
	}

	def documentAdd = {
		if (tagService.addDocumentTag(params.long("documentId"), params.tag)) {
			render(template:"/saved", model:[body: {g.message(code:"document-vault.tag.document.tagged", args:[params.documentId, params.tag])}])
			return
		}

		render(template:"/notsaved", model:[message:g.message(code:"document-vault.tag.document.nottagged", args:[params.documentId])])
		return
	}

	def documentList = {
		// IE caches GET requests. These lines prevent that
		response.setHeader('Last-Modified', "${now}")
		response.setHeader('Cache-Control', 'no-store, no-cache, must-revalidate, post-check=0, pre-check=0')
		response.setHeader('Pragma', 'No-cache')

		render (tagService.getDocumentTags(params.long("documentId")) as JSON)
	}

	def documentRemove = {
		def tag = params.tag?.trim()

		render ([status:tagService.removeDocumentTag(params.long("documentId"), tag) ? "success" : "error"] as JSON)
	}

	def documents = {
		def name = params.name?.trim()
		if (name) {
			render(template:"allTagged", model:[documents:tagService.findAllByTag(name), tag:name])
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
