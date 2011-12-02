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

	def create = {
		def group = Group.load(params.long('groupId'))
		assert group
		def parent = Folder.load(params.long('parentId'))

		def folder = folderService.createFolder(group, parent, params.name?.trim())
		def returnMap = [:]

		if (folder.hasErrors()) {
			returnMap.notification = notificationService.error('document-vault.api.folder.create.error')

			// TODO replace with collectEntries with Groovy 1.8.0
			returnMap.validation = [:].putAll(['group', 'name', 'documents', 'parent'].collect { field->
				new MapEntry(field,
					[
						errors:folder.errors.getFieldErrors(field).collect {
							g.message(error:it, encodeAs:'HTML')
						},
						valid:!folder.errors.hasFieldErrors(field)
					]
				)
			})
		} else {
			returnMap.notification = notificationService.success('document-vault.api.folder.create.success', [folder.name])
			returnMap.folder = folder.asMap()
		}

		render(returnMap as JSON)
	}

	def show = {
		def searchFolder = Folder.get(params.long('folderId'))
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

	def update = {
		def folder = Folder.get(params.long('folderId'))
		assert folder

		folder = folderService.renameFolder(folder, params.name?.trim())
		def returnMap = [:]

		if (folder.hasErrors()) {
			returnMap.notification = notificationService.error('document-vault.api.folder.update.error')

			// TODO replace with collectEntries with Groovy 1.8.0
			returnMap.validation = [:].putAll(['group', 'name', 'documents', 'parent'].collect { field->
				new MapEntry(field,
					[
						errors:folder.errors.getFieldErrors(field).collect {
							g.message(error:it, encodeAs:'HTML')
						},
						valid:!folder.errors.hasFieldErrors(field)
					]
				)
			})
		} else {
			returnMap.notification = notificationService.success('document-vault.api.folder.update.success', [folder.name])
			returnMap.folder = folder.asMap()
		}

		render(returnMap as JSON)
	}

	def delete = {
		def folder = Folder.get(params.long('folderId'))
		assert folder

		def folderName = folder.name
		folderService.deleteFolder(folder)

		render([notification:notificationService.success('document-vault.api.folder.delete.success', [folderName])] as JSON)
	}

	def list = {
		def searchFolder = Folder.load(params.long('folderId'))

		def pagination = [:]
		def max = params.int('max')
		pagination.max = max in 10..100 ? max : (max > 100 ? 100 : 10)
		pagination.sort = params.sort ?: 'name'
		pagination.order = params.order ?: 'asc'
		pagination.offset = params.int('offset') ?: 0

		def results = folderService.filter(searchFolder, pagination, params.filter?.trim())

		render([searchFolder:searchFolder?.asMap(), folders:results.results*.asMap(), folderTotal:results.total] as JSON)
	}

	def addDocument = {
		def destination = Folder.get(params.long('folderId'))
		assert destination
		def document = Document.get(params.long('documentId'))
		assert document
		assert document.folder?.id == params.long('currentFolderId')

		folderService.addDocumentToFolder(destination, document)

		render([notification:notificationService.success('document-vault.api.folder.addDocument.success', [document.name, destination.name])] as JSON)
	}

	def removeDocument = {
		def folder = Folder.get(params.long('folderId'))
		assert folder
		def document = Document.get(params.long('documentId'))
		assert document
		assert document.folder?.id == folder.id

		folderService.removeDocumentFromFolder(document)

		render([notification:notificationService.success('document-vault.api.folder.removeDocument.success', [document.name, folder.name])] as JSON)
	}

	def addFolder = {
		def parent = Folder.get(params.long('parentId'))
		assert parent
		def child = Folder.get(params.long('folderId'))
		assert child

		def currentParent = Folder.get(params.long('currentParentId'))
		assert currentParent == child.parent

		folderService.addChildToFolder(parent, child)

		render([notification:notificationService.success('document-vault.api.folder.addFolder.success', [child.name, parent.name])] as JSON)
	}

	def removeFolder = {
		def parent = Folder.get(params.long('parentId'))
		assert parent
		def child = Folder.get(params.long('folderId'))
		assert child

		assert parent == child.parent

		folderService.removeChildFromFolder(child)

		render([notification:notificationService.success('document-vault.api.folder.removeFolder.success', [child.name, parent.name])] as JSON)
	}

	def search = {
		def pagination = [:]
		def max = params.int('max')
		pagination.max = max in 10..100 ? max : (max > 100 ? 100 : 10)
		pagination.sort = params.sort ?: 'name'
		pagination.order = params.order ?: 'asc'
		pagination.offset = params.int('offset') ?: 0

		def results = folderService.search(pagination, params.filter?.trim())

		render([folders:results.results*.asMap(), folderTotal:results.total] as JSON)
	}

	def pin = {
		def folder = Folder.get(params.long('folderId'))

		folderService.pin(folder)

		def returnMap = [:]
		returnMap.notification = notificationService.success('document-vault.api.folder.pin.success', [folder.name])
		returnMap.folder = folder.asMap()

		render(returnMap as JSON)
	}

	def unpin = {
		def folder = Folder.get(params.long('folderId'))

		folderService.unpin(folder)

		def returnMap = [:]
		returnMap.notification = notificationService.success('document-vault.api.folder.unpin.success', [folder.name])

		render(returnMap as JSON)
	}

	def flag = {
		def folder = Folder.get(params.long('folderId'))
		assert folder
		def flag = params.flag
		assert flag && tenantService.getTenantConfigList('flag').contains(flag)

		assert authService.canManageFolders(folder.group)

		folder.addTag(flag)
		folder.save()

		def returnMap = [:]
		returnMap.notification = notificationService.success('document-vault.api.folder.flag.success', [folder.name, flag])
		returnMap.folder = folder.asMap()

		render(returnMap as JSON)
	}

	def unflag = {
		def folder = Folder.get(params.long('folderId'))
		assert folder
		def flag = params.flag
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
