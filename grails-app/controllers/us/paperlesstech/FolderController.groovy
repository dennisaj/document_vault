package us.paperlesstech

import grails.converters.JSON
import us.paperlesstech.nimble.Group

class FolderController {
	static def allowedMethods = [create: 'POST', delete: 'POST', update: 'POST', addDocument: 'POST', removeDocument: 'POST', addFolder: 'POST',
			pin: 'POST', unpin: 'POST', show: 'GET']

	def authService
	def documentService
	def folderService
	def notificationService
	def tenantService

	def create(Long groupId, String name, Long folderId) {
		def group = Group.get(groupId)
		assert group
		def parent = Folder.get(folderId)

		def folder = folderService.createFolder(group, name?.trim(), parent)
		def returnMap = [:]

		if (folder.hasErrors()) {
			returnMap.notification = notificationService.error('document-vault.api.folder.create.error')

			returnMap.validation = ['group', 'name', 'documents', 'parent'].collectEntries { field->
				[(field):
					[
						errors:folder.errors.getFieldErrors(field).collect {
							g.message(error:it, encodeAs:'HTML')
						},
						valid:!folder.errors.hasFieldErrors(field)
					]
				]
			}
		} else {
			returnMap.notification = notificationService.success('document-vault.api.folder.create.success', [folder.name])
			returnMap.folder = folder.asMap()
		}

		render(returnMap as JSON)
	}

	def show(Long folderId) {
		def searchFolder = Folder.get(folderId)
		assert searchFolder

		def pagination = [:]
		pagination.max = 10
		pagination.sort = 'name'
		pagination.order = 'asc'
		pagination.offset = 0

		def folderResults = folderService.filter(searchFolder, pagination, null)

		pagination.sort = 'dateCreated'
		pagination.order = 'desc'
		def docResults = documentService.filter(searchFolder, pagination, null)

		def ancestry = folderService.ancestry(searchFolder)
		def searchFolderMap = searchFolder.asMap()
		searchFolderMap.ancestry = ancestry*.asMap()

		render([searchFolder: searchFolderMap,
				folders: folderResults.results*.asMap(),
				folderTotal: folderResults.total,
				documents: docResults.results*.asMap(),
				documentTotal: docResults.total] as JSON)
	}

	def update(Long folderId, String name) {
		def folder = Folder.get(folderId)
		assert folder

		folder = folderService.renameFolder(folder, name?.trim())
		def returnMap = [:]

		if (folder.hasErrors()) {
			returnMap.notification = notificationService.error('document-vault.api.folder.update.error')

			returnMap.validation = ['group', 'name', 'documents', 'parent'].collectEntries { field->
				[(field):
					[
						errors:folder.errors.getFieldErrors(field).collect {
							g.message(error:it, encodeAs:'HTML')
						},
						valid:!folder.errors.hasFieldErrors(field)
					]
				]
			}
		} else {
			returnMap.notification = notificationService.success('document-vault.api.folder.update.success', [folder.name])
			returnMap.folder = folder.asMap()
		}

		render(returnMap as JSON)
	}

	def delete(Long folderId) {
		def folder = Folder.get(folderId)
		assert folder

		def folderName = folder.name
		folderService.deleteFolder(folder)

		render([notification:notificationService.success('document-vault.api.folder.delete.success', [folderName])] as JSON)
	}

	def list(Long folderId, String filter, Integer max, String sort, String order, Integer offset) {
		def searchFolder = Folder.load(folderId)

		def pagination = [:]
		pagination.max = max in 10..100 ? max : (max > 100 ? 100 : 10)
		pagination.sort = sort ?: 'name'
		pagination.order = order ?: 'asc'
		pagination.offset = offset ?: 0

		def results = folderService.filter(searchFolder, pagination, filter?.trim())

		render([searchFolder:searchFolder?.asMap(), folders:results.results*.asMap(), folderTotal:results.total] as JSON)
	}

	def addDocument(Long folderId, Long documentId, Long currentFolderId) {
		def destination = Folder.get(folderId)
		assert destination
		def document = Document.get(documentId)
		assert document
		assert document.folder?.id == currentFolderId

		folderService.addDocumentToFolder(destination, document)

		render([notification:notificationService.success('document-vault.api.folder.addDocument.success', [document.name, destination.name])] as JSON)
	}

	def removeDocument(Long folderId, Long documentId) {
		def folder = Folder.get(folderId)
		assert folder
		def document = Document.get(documentId)
		assert document
		assert document.folder?.id == folder.id

		folderService.removeDocumentFromFolder(document)

		render([notification:notificationService.success('document-vault.api.folder.removeDocument.success', [document.name, folder.name])] as JSON)
	}

	def addFolder(Long parentId, Long childId, Long currentParentId) {
		def parent = Folder.get(parentId)
		assert parent
		def child = Folder.get(childId)
		assert child

		def currentParent = Folder.get(currentParentId)
		assert currentParent == child.parent

		folderService.addChildToFolder(parent, child)

		render([notification:notificationService.success('document-vault.api.folder.addFolder.success', [child.name, parent.name])] as JSON)
	}

	def removeFolder(Long parentId, Long folderId) {
		def parent = Folder.get(parentId)
		assert parent
		def child = Folder.get(folderId)
		assert child

		assert parent == child.parent

		folderService.removeChildFromFolder(child)

		render([notification:notificationService.success('document-vault.api.folder.removeFolder.success', [child.name, parent.name])] as JSON)
	}

	def search(String filter, Integer max, String sort, String order, Integer offset) {
		def pagination = [:]
		pagination.max = max in 10..100 ? max : (max > 100 ? 100 : 10)
		pagination.sort = sort ?: 'name'
		pagination.order = order ?: 'asc'
		pagination.offset = offset ?: 0

		def results = folderService.search(pagination, filter?.trim())

		render([folders:results.results*.asMap(), folderTotal:results.total] as JSON)
	}

	def pin(Long folderId) {
		def folder = Folder.get(folderId)

		folderService.pin(folder)

		def returnMap = [:]
		returnMap.notification = notificationService.success('document-vault.api.folder.pin.success', [folder.name])
		returnMap.folder = folder.asMap()

		render(returnMap as JSON)
	}

	def unpin(Long folderId) {
		def folder = Folder.get(folderId)

		folderService.unpin(folder)

		def returnMap = [:]
		returnMap.notification = notificationService.success('document-vault.api.folder.unpin.success', [folder.name])

		render(returnMap as JSON)
	}

	def flag(Long folderId, String flag) {
		def folder = Folder.get(folderId)
		assert folder
		assert flag && tenantService.getTenantConfigList('flag').contains(flag)

		assert authService.canManageFolders(folder.group)

		folder.addTag(flag)
		folder.save()

		def returnMap = [:]
		returnMap.notification = notificationService.success('document-vault.api.folder.flag.success', [folder.name, flag])
		returnMap.folder = folder.asMap()

		render(returnMap as JSON)
	}

	def unflag(Long folderId, String flag) {
		def folder = Folder.get(folderId)
		assert folder
		assert flag && tenantService.getTenantConfigList('flag').contains(flag)

		assert authService.canManageFolders(folder.group)

		if(folder.tags.contains(flag)) {
			folder.removeTag(flag)
			folder.save()
		}

		def returnMap = [:]
		returnMap.notification = notificationService.success('document-vault.api.folder.unflag.success', [folder.name, flag])
		returnMap.folder = folder.asMap()

		render(returnMap as JSON)
	}
}
