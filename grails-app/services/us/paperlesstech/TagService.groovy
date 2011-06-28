package us.paperlesstech

import grails.plugin.multitenant.core.util.TenantUtils;

import org.apache.commons.lang.StringEscapeUtils
import org.grails.taggable.Tag

class TagService {

	static transactional = true

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
		return Tag.list([max:5, sort:'id', order:'desc'])
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
		def currentTenant = TenantUtils.currentTenant
		String findByTagHQL = """
			SELECT document from Document document
			WHERE document.tenantId = $currentTenant AND document.id NOT IN(SELECT tagLink.tagRef FROM TagLink tagLink where tagLink.type = 'document' GROUP BY tagLink.tagRef)
			ORDER BY document.id
		"""

		return Document.executeQuery(findByTagHQL)
	}
}
