package us.paperlesstech

import org.hibernate.criterion.DetachedCriteria
import org.hibernate.criterion.Projections
import org.hibernate.criterion.Restrictions
import org.hibernate.criterion.Subqueries
import org.hibernate.sql.JoinFragment

import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.User

class FolderService {

	static transactional = true

	def authService

	/**
	 * Returns the ancestry of the given folder.  This is all of the parents up to the root.
	 *
	 * @param folder The folder to find the parents for
	 * @return A list of Folders with the highest level first
	 */
	List<Folder> ancestry(Folder folder) {
		def folders = []

		while (folder.parent) {
			folder = folder.parent
			folders << folder
		}

		folders.reverse()
	}

	/**
	 * Creates a new folder with given name in the given Group. The new folder's parent is set if parent is not null.
	 *
	 * @pre The current user must have the {@link DocumentPermission#ManageFolders} permission for group.
	 * @throws RuntimeException if there is a problem saving
	 */
	Folder createFolder(Group group, String name, Folder parent=null) {
		assert group
		assert authService.canManageFolders(group)
		assert !parent || parent.group == group

		def folder = new Folder(group:group, name:name, parent:parent)

		if (!folder.validate()) {
			log.debug 'Folder failed validation'
			return folder
		}

		parent?.addToChildren(folder)
		parent?.save(failOnError:true)

		folder.save(failOnError:true)
	}

	/**
	 * Removes all documents and folder from the given folder and deletes it.
	 *
	 * @pre The current user must have the {@link DocumentPermission#ManageFolders} permission for document.
	 * @throws RuntimeException if there is a problem saving
	 */
	def deleteFolder(Folder folder) {
		assert folder
		assert authService.canManageFolders(folder.group)

		if (folder.documents) {
			def documents = []
			documents.addAll(folder.documents)
			documents.each {
				folder.removeFromDocuments(it)
			}
		}

		if (folder.children) {
			def children = []
			children.addAll(folder.children)
			children.each {
				folder.removeFromChildren(it)
			}
		}

		folder.parent?.removeFromChildren(folder)
		folder.parent?.save(failOnError:true)

		folder.delete(flush:true)
	}

	def renameFolder(Folder folder, String name) {
		assert folder
		assert authService.canManageFolders(folder.group)

		folder.name = name

		if (!folder.validate()) {
			log.debug 'Folder failed validation'
			return folder
		}

		folder.save(failOnError:true)
	}

	/**
	 * Moves the given document out of its current folder (if applicable)
	 * and then moves it into the given folder.
	 *
	 * @pre The current user must have the {@link DocumentPermission#ManageFolders} permission for document.group.
	 * @pre destination.group must equal document.group
	 * @throws RuntimeException if there is a problem saving
	 */
	Document addDocumentToFolder(Folder destination, Document document) {
		assert destination
		assert document

		if (document.folder == destination) {
			return document
		}

		assert document.group == destination.group
		assert authService.canManageFolders(destination.group)

		document.folder?.removeFromDocuments(document)
		destination.addToDocuments(document)
		destination.save(failOnError:true)
		document
	}

	/**
	 * Removes of the document from its current folder and sets its folder to null.
	 *
	 * If document.folder is null, nothing happens.
	 *
	 * @pre The current user must have the {@link DocumentPermission#ManageFolders} permission for document.group.
	 * @throws RuntimeException if there is a problem saving
	 */
	Document removeDocumentFromFolder(Document document) {
		assert document
		assert authService.canManageFolders(document.group)

		def folder = document.folder
		folder?.removeFromDocuments(document)
		folder?.save(failOnError:true)
		document
	}

	/**
	 * Moves the given child out of its current parent (if applicable)
	 * and then moves it into the given parent.
	 *
	 * @pre The current user must have the {@link DocumentPermission#ManageFolders} permission for document.group.
	 * @pre parent.group must equal child.group
	 * @throws RuntimeException if there is a problem saving
	 * @throws grails.validation.ValidationException if the parent is a descendant of child.
	 */
	Folder addChildToFolder(Folder parent, Folder child) {
		assert parent
		assert child

		if (child.parent == parent) {
			return child
		}

		assert child.group == parent.group
		assert authService.canManageFolders(parent.group)

		child.parent?.removeFromChildren(child)
		child.parent?.save(failOnError: true)

		parent.addToChildren(child)
		parent.save(failOnError: true)
		child.parent = parent
		child.save(failOnError: true)
		child
	}

	/**
	* Sets the given child's parent to null and remove the child from the parent.
	*
	* @pre The current user must have the {@link DocumentPermission#ManageFolders} permission for child.group.
	* @throws RuntimeException if there is a problem saving
	*/
	Folder removeChildFromFolder(Folder child) {
		assert child
		assert authService.canManageFolders(child.group)

		if (child.parent == null) {
			return child
		}

		def parent = child.parent
		parent.removeFromChildren(child)
		parent.save(failOnError:true)
		child.parent = null
		child.save(failOnError: true)
		child
	}

	/**
	 * Pins the passed folder for the current user
	 *
	 * @param folder The folder to pin
	 */
	void pin(Folder folder) {
		User user = authService.authenticatedUser

		def pinned = PinnedFolder.findByFolderAndUser(folder, user)
		if (pinned) {
			return
		}

		pinned = new PinnedFolder(folder: folder, user: user)
		pinned.save(failOnError: true)
	}

	/**
	 * Unpins the passed folder from the current user
	 *
	 * @param folder The folder to unpin
	 */
	void unpin(Folder folder) {
		User user = authService.authenticatedUser

		def pinned = PinnedFolder.findByFolderAndUser(folder, user)
		if (!pinned) {
			return
		}

		pinned.delete()
	}

	/**
	 * List all of the folders a user can view in the given parent Folder.
	 * If filter is set, apply that filter to the name field of the folders <br><br>
	 * @param params A {@link Map} containing the pagination information
	 * @return a {@link Map} with two entries: results and total.
	 * Results is a list of paginated folders and total is the total count for the query
	 */
	def filter(Folder parent=null, Map params, String filter) {
		commonQuery(parent, params, filter, true)
	}

	def search(Map params, String filter) {
		commonQuery(params, filter, false)
	}

	private def commonQuery(Folder parent=null, Map params, String filter, boolean useParent) {
		def groupPerms = [DocumentPermission.ManageFolders, DocumentPermission.GetSigned, DocumentPermission.Sign, DocumentPermission.View]

		// If the user has permission to view the whole parent, paginate all folders.
		if (parent && groupPerms.any { authService.checkGroupPermission(it, parent.group) }) {
			return [results:Folder.createCriteria().list {
					eq 'parent', parent
					if (filter) {
						ilike 'name', "%$filter%"
					}
					maxResults(params.max)
					firstResult(params.offset)
					order(params.sort, params.order)
				}, total:parent.children?.size()]
		}

		// If not, find the individual folders in this parent that the user can view.
		def specificGroups = authService.getGroupsWithPermission(groupPerms) ?: [null]

		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Folder.class)
			.setProjection(Projections.distinct(Projections.id()))
			.add(Restrictions.in('group', specificGroups))

		if (useParent) {
			if (parent) {
				detachedCriteria.add(Restrictions.eq('parent', parent))
			} else {
				detachedCriteria.add(Restrictions.isNull('parent'))
			}
		}

		if (filter) {
			detachedCriteria.add(Restrictions.ilike('name', "%$filter%"))
		}

		def total = Folder.createCriteria().count {
			addToCriteria(Subqueries.propertyIn('id', detachedCriteria))
		}

		def folders = Folder.createCriteria().list {
			addToCriteria(Subqueries.propertyIn('id', detachedCriteria))
			maxResults(params.max)
			firstResult(params.offset)
			order(params.sort, params.order)
		}

		[results:folders, total:total]
	}
}
