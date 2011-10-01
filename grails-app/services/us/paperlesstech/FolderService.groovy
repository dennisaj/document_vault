package us.paperlesstech

import us.paperlesstech.nimble.Group

class FolderService {

	static transactional = true

	def authService

	/**
	 * Creates a new folder with given name and puts the document into it.
	 * If the folder already exists, the document is added and the folder is returned.
	 *
	 * @pre The current user must have the {@link DocumentPermission#FolderCreate} permission for initialDocument.
	 * @throws RuntimeException if there is a problem saving
	 */
	Folder createFolder(Group group, String name, Document initialDocument) {
		assert group
		assert initialDocument?.group == group
		assert authService.canFolderCreate(initialDocument.group)
		assert !initialDocument.folder || authService.canFolderMoveOutOf(initialDocument.group)

		initialDocument.folder?.removeFromDocuments(initialDocument)

		def folder = new Folder(group:group, name:name)
		folder.addToDocuments(initialDocument)

		if (!folder.validate()) {
			log.debug "Folder failed validation"
			return folder
		}

		folder.save(failOnError:true)
	}

	/**
	 * Removes all documents from the given folder and deletes it.
	 *
	 * @pre The current user must have the {@link DocumentPermission#FolderDelete} permission for document.
	 * @throws RuntimeException if there is a problem saving
	 */
	def deleteFolder(Folder folder) {
		assert folder
		assert authService.canFolderDelete(folder.group)

		if (folder.documents) {
			def documents = []
			documents.addAll(folder.documents)
			documents.each {
				folder.removeFromDocuments(it)
			}
		}

		folder.delete(flush:true)
	}

	/**
	 * Moves the given document out of its current folder (if applicable)
	 * and then moves it into the given folder.
	 *
	 * @pre The current user must have the {@link DocumentPermission#FolderMoveInTo} permission for document.group.
	 * If document.folder is set, the user must also have {@link DocumentPermission#FolderMoveOutOf} permission for the current folder.
	 * @pre folder.group must equal document.group
	 * @throws RuntimeException if there is a problem saving
	 */
	Document addDocumentToFolder(Folder destination, Document document) {
		assert destination
		assert document

		if (document.folder == destination) {
			return document
		}

		assert document.group == destination.group
		assert authService.canFolderMoveInTo(destination.group)
		assert !document.folder || authService.canFolderMoveOutOf(document.group)

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
	 * @pre The current user must have the {@link DocumentPermission#FolderMoveOutOf} permission for document.group.
	 * @throws RuntimeException if there is a problem saving
	 */
	Document removeDocumentFromFolder(Document document) {
		assert document
		assert !document.folder || authService.canFolderMoveOutOf(document.group)

		def folder = document.folder
		folder?.removeFromDocuments(document)
		folder?.save(failOnError:true)
		document
	}
}
