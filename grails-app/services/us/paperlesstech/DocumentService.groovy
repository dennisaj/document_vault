package us.paperlesstech

import org.hibernate.criterion.DetachedCriteria
import org.hibernate.criterion.Projections
import org.hibernate.criterion.Restrictions
import org.hibernate.criterion.Subqueries
import org.hibernate.sql.JoinFragment

class DocumentService {
	static transactional = true

	def authService
	def tagService

	def search(params) {
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
				.createAlias("notes", "n", JoinFragment.LEFT_OUTER_JOIN)
				.setProjection(Projections.distinct(Projections.id()))
				.add(Restrictions.or(Restrictions.in("id", specificDocs), Restrictions.in("group.id", allowedGroupIds)))
			if (params.q) {
				detachedCriteria.add(Restrictions.disjunction().add(Restrictions.ilike("name", "%$params.q%"))
						.add(Restrictions.ilike("sfc.value", "%$params.q%"))
						.add(Restrictions.ilike("n.note", "%$params.q%")))
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

		[tagSearchResults:tagSearchResults, documentResults:documentResults, documentTotal:documentTotal]
	}
}
