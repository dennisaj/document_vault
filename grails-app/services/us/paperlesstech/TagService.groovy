package us.paperlesstech

import org.apache.commons.lang.StringEscapeUtils
import org.grails.taggable.Tag
import org.grails.taggable.TagLink
import org.hibernate.criterion.DetachedCriteria
import org.hibernate.criterion.Projections
import org.hibernate.criterion.Restrictions
import org.hibernate.criterion.Subqueries

class TagService {
	static transactional = true

	def authService

	boolean addDocumentTag(long documentId, tag) {
		return addDocumentTag(Document.get(documentId), tag)
	}

	boolean addDocumentTag(Document document, tag) {
		tag = sanitize(tag)
		if (tag && document) {
			document.addTag(tag)
			return true
		}

		return false
	}

	/**
	 *
	 * @return A String that has been striped of commas and trailing slashes
	 */
	private String sanitize(String tag) {
		def clean = tag?.trim() ?: ""
		while (clean.endsWith("/")) {
			clean = clean.substring(0, clean.size() - 1)
		}

		clean = clean.replace(",", "")
		clean = StringEscapeUtils.unescapeHtml(clean)

		return clean
	}

	/**
	 * This method will create a new tag that has not been assigned to any rows.
	 *
	 * This method should only be called if you don't plan on assigning this tag to any rows immediately.
	 * Otherwise, use the injected addTag method for the row you are tagging.
	 *
	 * @return The saved tag or a tag containing errors
	 */
	Tag createTag(name) {
		def tag = new Tag(name:sanitize(name))

		def savedTag = tag.save()
		return savedTag ?: tag
	}

	/**
	 *
	 * @return a list of tag names associated with a document that have had any html escaped.
	 * If the documentId is invalid, an empty list is returned.
	 */
	List<String> getDocumentTags(documentId) {
		def document = Document.get(documentId)
		if (document) {
			return document.getTags().collect({StringEscapeUtils.escapeHtml(it)})
		} else {
			return []
		}
	}

	List<String> getRecentTags() {
		def tags = Tag.list([max:5, sort:'id', order:'desc'])
		return tags*.name
	}

	List findAllByTag(String name) {
		def allowedGroupIds = authService.getGroupsWithPermission([DocumentPermission.Tag]).collect { it.id } ?: -1L
		def specificDocs = authService.getIndividualDocumentsWithPermission([DocumentPermission.Tag]) ?: -1L

		Document.findAllByTagWithCriteria(name) {
			or {
				inList("id", specificDocs)
				inList("group.id", allowedGroupIds)
			}
		}
	}

	boolean removeDocumentTag(def documentId, tag) {
		return removeDocumentTag(Document.get(documentId), tag)
	}

	boolean removeDocumentTag(Document document, tag) {
		if (document) {
			document.removeTag(tag)
			return true
		}

		return false
	}

	List<String> tagSearch(final List<String> tags) {
		def c = Tag.createCriteria()

		return c {
			maxResults(20)
			or {
				tags.each {
					if (it) {
						like("name", "%" + it.toLowerCase() + "%")
					}
				}
			}
			order('name', 'asc')
		}*.name
	}

	List<Document> untaggedDocuments() {
		def c = Document.createCriteria()
		def allowedGroupIds = authService.getGroupsWithPermission([DocumentPermission.Tag]).collect { it.id } ?: -1L
		def specificDocs = authService.getIndividualDocumentsWithPermission([DocumentPermission.Tag]) ?: -1L

		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(TagLink.class)
				.add(Restrictions.eq("type", "document"))
				.setProjection(Projections.groupProperty("tagRef"))

		c.list {
			addToCriteria(Subqueries.propertyNotIn("id", detachedCriteria))
			or {
				inList("id", specificDocs)
				inList("group.id", allowedGroupIds)
			}
		}
	}
}
