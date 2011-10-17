package us.paperlesstech

import org.hibernate.criterion.DetachedCriteria
import org.hibernate.criterion.Projections
import org.hibernate.criterion.Restrictions
import org.hibernate.criterion.Subqueries
import org.hibernate.sql.JoinFragment

class DocumentService {
	static transactional = true

	def authService

	def search(Folder folder=null, Map params, String filter) {
		def allowedGroupIds = authService.getGroupsWithPermission([DocumentPermission.GetSigned, DocumentPermission.Sign, DocumentPermission.View]).collect { it.id } ?: -1L
		def specificDocs = authService.getIndividualDocumentsWithPermission([DocumentPermission.GetSigned, DocumentPermission.Sign, DocumentPermission.View]) ?: -1L

		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Document.class)
			.createAlias('searchFieldsCollection', 'sfc', JoinFragment.LEFT_OUTER_JOIN)
			.createAlias('notes', 'n', JoinFragment.LEFT_OUTER_JOIN)
			.setProjection(Projections.distinct(Projections.id()))
			.add(Restrictions.or(Restrictions.in('id', specificDocs), Restrictions.in('group.id', allowedGroupIds)))

		if (filter) {
			detachedCriteria.add(Restrictions.disjunction().add(Restrictions.ilike('name', "%$filter%"))
					.add(Restrictions.ilike('sfc.value', "%$filter%"))
					.add(Restrictions.ilike('n.note', "%$filter%")))
		}

		if (folder) {
			detachedCriteria.add(Restrictions.eq('folder', folder))
		} else {
			detachedCriteria.add(Restrictions.isNull('folder'))
		}

		def documentTotal = Document.createCriteria().count {
			addToCriteria(Subqueries.propertyIn('id', detachedCriteria))
		}

		def documentResults = Document.createCriteria().list {
			addToCriteria(Subqueries.propertyIn('id', detachedCriteria))
			maxResults(params.max)
			firstResult(params.offset)
			order(params.sort, params.order)
		}

		[documentResults:documentResults, documentTotal:documentTotal]
	}
}
