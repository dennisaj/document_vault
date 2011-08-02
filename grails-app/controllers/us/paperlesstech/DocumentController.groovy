package us.paperlesstech

import grails.converters.JSON

import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.criterion.DetachedCriteria
import org.hibernate.criterion.Projections
import org.hibernate.criterion.Restrictions
import org.hibernate.criterion.Subqueries
import org.hibernate.sql.JoinFragment

class DocumentController {
	static allowedMethods = [addParty:"POST", image:"POST", removeParty:"POST", resend:"POST", saveNote:"POST", submitParties:"POST", submitSignatures:"POST"]
	static navigation = [[action:'index', isVisible: { authService.isLoggedIn() }, order:0, title:'Home']]

	def authService
	def handlerChain
	def partyService
	def sessionFactory
	def tagService

	def index = {
		def documentResults = []
		def documentTotal = 0
		def tagSearchResults = []

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
			tagSearchResults = [tag]
		} else if (terms?.size()) {
			tagSearchResults = tagService.tagSearch(terms)
		} else {
			tagSearchResults = tagService.getRecentTags()
		}

		def allowedGroupIds = authService.getGroupsWithPermission([DocumentPermission.GetSigned, DocumentPermission.Sign, DocumentPermission.View]).collect { it.id } ?: -1L
		def specificDocs = authService.getIndividualDocumentsWithPermission([DocumentPermission.GetSigned, DocumentPermission.Sign, DocumentPermission.View]) ?: -1L

		if (tagSearch) {
			documentResults += Document.findAllByTagWithCriteria(tag) {
				or {
					inList("id", specificDocs)
					inList("group.id", allowedGroupIds)
				}
				maxResults(params.max)
				firstResult(params.offset)
				order(params.sort, params.order)
			}
		} else {
			DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Document.class)
				.createAlias("searchFieldsCollection", "sfc", JoinFragment.LEFT_OUTER_JOIN)
				.setProjection(Projections.distinct(Projections.id()))
				.add(Restrictions.or(Restrictions.in("id", specificDocs), Restrictions.in("group.id", allowedGroupIds)))
			if (params.q) {
				detachedCriteria.add(Restrictions.or(Restrictions.ilike("name", "%$params.q%"), Restrictions.ilike("sfc.value", "%$params.q%")))
			}

			documentTotal = Document.createCriteria().count {
				addToCriteria(Subqueries.propertyIn("id", detachedCriteria))
			}

			def c = Document.createCriteria()

			documentResults += c.list {
				addToCriteria(Subqueries.propertyIn("id", detachedCriteria))
				maxResults(params.max)
				firstResult(params.offset)
				order(params.sort, params.order)
			}
		}

		def model = [tagSearchResults:tagSearchResults, documentResults:documentResults, documentTotal:documentTotal]
		if (request.xhr) {
			render(template: "searchResults", model: model)
		} else {
			model
		}
	}

	def note = {
		def document = Document.get(params.long('documentId'))
		if (document) {
			assert authService.canNotes(document)

			render text:document.searchField("Note") ?: "", contentType: "text/plain"
			return
		}

		render ([status:"error"] as JSON)
	
	}

	def saveNote = {
		def document = Document.get(params.long('documentId'))
		if (document) {
			assert authService.canNotes(document)
			document.searchField("Note", params.value)
			document.save(flush:true)

			render text:document.searchField("Note")?.encodeAsHTML(), contentType: "text/plain"
			return
		}

		render ([status:"error"] as JSON)
	}

	def downloadImage = {
		def document = Document.get(params.long("documentId"))
		if (!document) {
			response.status = 404
			return
		}

		def (filename, is, contentType, length) = handlerChain.downloadPreview(document: document, page: params.pageNumber?.toInteger() ?: 1)
		is.withStream {
			response.setContentType(contentType)
			response.setContentLength(length)
			response.getOutputStream() << is
		}
	}

	def download = {
		def document = Document.get(params.long("documentId"))
		def documentData = document?.files?.find { it.id == params.long("documentDataId")}
		if (!document || !documentData) {
			response.status = 404
			return
		}

		cache neverExpires: true

		def (filename, is, contentType, length) = handlerChain.download(document: document, documentData: documentData)
		is.withStream {
			response.setContentType(contentType)
			response.setContentLength(length)
			response.setHeader("Content-Disposition", "attachment; filename=${filename}")
			response.getOutputStream() << is
		}
	}

	def thumbnail = {
		def document = Document.get(params.long("documentId"))
		def documentData = document?.previewImages?.find { it.thumbnail.id == params.long("documentDataId") }
		if (!document || !documentData) {
			response.status = 404
			return
		}

		cache neverExpires: true

		def (filename, is, contentType, length) = handlerChain.downloadThumbnail(document: document, page: params.pageNumber?.toInteger() ?: 1)
		is.withStream {
			response.setContentType(contentType)
			response.setContentLength(length)
			response.setHeader("Content-Disposition", "attachment; filename=${filename}")
			response.getOutputStream() << is
		}
	}

	def show = {
		def document = Document.get(params.long("documentId"))
		assert document

		[document: document]
	}

	def image = {
		def d = Document.get(params.long("documentId"))
		def pageNumber = params.int("pageNumber");
		assert d

		def map = d.previewImageAsMap(pageNumber)
		map.highlights = (authService.canGetSigned(d) || authService.canSign(d) ? d.highlightsAsMap(pageNumber) : [:])

		render(map as JSON)
	}

	def sign = {
		def document = Document.get(params.long("documentId"))
		assert document

		[document: document, parties:Party.findAllByDocument(document), colors:PartyColor.values(), permissions:Party.allowedPermissions]
	}

	def submitSignatures = {
		def document = Document.get(params.long("documentId"))
		assert document

		def signatures = JSON.parse(params?.lines).findAll {it.value}

		if (signatures) {
			document = partyService.cursiveSign(document, signatures)
			if (!document.hasErrors()) {
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

	// TODO: Move functions to party controller
	def addParty = {
		def document = Document.get(params.long("documentId"))
		assert document

		render template:"party", model:[document:document, colors:PartyColor.values(), permissions:Party.allowedPermissions, party:new Party()]
	}

	def removeParty = {
		def document = Document.get(params.long("documentId"))
		def party = Party.get(params.long("partyId"))
		assert document
		assert party
		assert document == party.document

		partyService.removeParty(party)

		render ([status:"success"] as JSON)
	}

	def submitParties = {
		def document = Document.get(params.long("documentId"))
		assert document
		def inParties = JSON.parse(params?.parties)

		def outParties = inParties.collect {inParty->
			// Remove JSONObject.Null entries
			// TODO replace with collectEntries with Groovy 1.8.0
			inParty = [:].putAll(inParty.collect {k,v->
				if (v == JSONObject.NULL) {
					v = null
				}

				new MapEntry(k,v)
			})

			def outParty = null
			if (inParty.id) {
				def party = Party.get(inParty.id as long)

				if (party) {
					outParty = partyService.updateHighlights(party, inParty.highlights)
				}
			} else {
				outParty = partyService.createParty(document, inParty)
				if (outParty.hasErrors() || outParty.signator.hasErrors()) {
					// If there was an error, use the existing code. 
					// This ensures that the unsaved clientside highlights won't disappear. 
					outParty.code = inParty.code
				}
			}

			outParty
		}

		render template:"parties", model:[colors:PartyColor.values(), document:document, permissions:Party.allowedPermissions, parties:outParties]
	}

	def resend = {
		assert params.documentId
		assert params.partyId

		def document = Document.get(params.long("documentId"))
		def party = Party.get(params.long("partyId"))

		assert document == party.document

		partyService.sendCode(party)

		render([status:"success"] as JSON)
	}
}
