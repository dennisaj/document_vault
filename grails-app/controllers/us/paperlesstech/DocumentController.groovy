package us.paperlesstech

import grails.converters.JSON

class DocumentController {
	static allowedMethods = [finalize: "GET", image: "GET", savePcl: "POST"]
	static navigation = [[action:'index', isVisible: { authService.isLoggedIn() }, order:0, title:'Home']]

	def authService
	def handlerChain
	def tagService

	def index = {
		params.q = params.q?.trim()
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		params.offset = params.offset ? params.int('offset') : 0
		params.sort = params.sort ?: "dateCreated"
		params.order = params.order ?: "desc"

		def tagSearch = false
		def tag
		if (params.q?.startsWith("tagged")) {
			tagSearch = true
			tag = params.q.substring("tagged".size()).trim()
		}

		def terms = params.q?.tokenize(",").collect { it.trim() }.findAll { it }
		if (tagSearch) {
			params.tagSearchResults = [tag]
		} else if (terms?.size()) {
			params.tagSearchResults = tagService.tagSearch(terms)
		} else {
			params.tagSearchResults = tagService.getRecentTags()
		}

		def allowedGroupIds = authService.getGroupsWithPermission(DocumentPermission.View).collect { it.id } ?: -1L
		def specificDocs = authService.getIndividualDocumentsWithPermission(DocumentPermission.View) ?: -1L

		if (tagSearch) {
			params.documents = Document.findAllByTagWithCriteria(tag) {
				or {
					inList("id", specificDocs)
					inList("group.id", allowedGroupIds)
				}
				maxResults(params.max)
				firstResult(params.offset)
				order(params.sort, params.order)
			}
		} else {
			def c = Document.createCriteria()
			params.documents = c.listDistinct {
				or {
					inList("id", specificDocs)
					inList("group.id", allowedGroupIds)
				}
				if (params.q) {
					or {
						ilike("name", "%$params.q%")
						searchFieldsCollection {
							ilike("value", "%$params.q%")
						}
					}
				}
				maxResults(params.max)
				firstResult(params.offset)
				order(params.sort, params.order)
			}
		}

		if (request.xhr) {
			render(template: "searchResults", model: params)
		} else {
			params
		}
	}

	def saveNote = {
		def document = Document.get(params.long('documentId'))
		if (document) {
			assert authService.canNotes(document)
			document.searchField("Note", params.value)
			document.save(flush:true)

			render text:params.value, contentType: "text/plain"
			return
		}

		render ([status:"error"] as JSON)
	}

	def downloadImage = {
		def document = Document.get(params.long("documentId"))
		if (document) {
			def (filename, data, contentType) = handlerChain.downloadPreview(document: document, page: params.pageNumber?.toInteger() ?: 1)
			response.setContentType(contentType)
			response.setContentLength(data.length)
			response.getOutputStream().write(data)
		}

		response.status = 404
	}

	def download = {
		def document = Document.get(params.long("documentId"))
		if (document) {
			def (filename, data, contentType) = handlerChain.download(document: document, documentData: document.files.first())
			response.setContentType(contentType)
			response.setContentLength(data.length)
			response.setHeader("Content-Disposition", "attachment; filename=${filename}")
			response.getOutputStream().write(data)
		}

		response.status = 404
	}

	def show = {
		def document = Document.get(params.long("documentId"))
		assert document

		[document: document]
	}

	def image = {
		def d = Document.get(params.long("documentId"))
		assert d

		def map = d.previewImageAsMap(params.int("pageNumber"))
		render(map as JSON)
	}

	def sign = {
		def document = Document.get(params.long("documentId"))
		assert document

		[document: document]
	}

	def submitSignatures = {
		def document = Document.get(params.long("documentId"))
		assert document

		def signatures = JSON.parse(params?.lines).findAll {it.value}

		if (signatures) {
			handlerChain.sign(document: document, documentData: document.files.first(), signatures:signatures)
			if (document.save()) {
				flash.green = g.message(code:"document-vault.signature.success")
			} else {
				document.errors.each {
					log.error "[Document(${document.id})] - " + it
				}

				flash.red = g.message(code:"document-vault.signature.error.failure")
			}
		} else {
			flash.yellow = g.message(code:"document-vault.signature.error.nosignatures")
		}

		render ([status:"success"] as JSON)
	}
}
