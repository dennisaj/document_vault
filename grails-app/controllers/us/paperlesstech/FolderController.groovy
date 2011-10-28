package us.paperlesstech

import grails.converters.JSON
import us.paperlesstech.nimble.Group

class FolderController {
	def folderService
	def notificationService

	def create = {
		def group = Group.load(params.long('groupId'))
		assert group
		def parent = Folder.load(params.long('parentId'))

		def folder = folderService.createFolder(group, parent, params.name?.trim())
		def returnMap = [:]

		if (folder.hasErrors()) {
			returnMap.notification = notificationService.error('document-vault.api.folder.create.error')

			// TODO replace with collectEntries with Groovy 1.8.0
			returnMap.validation = [:].putAll(['group', 'name', 'documents'].collect { field->
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

		def results = folderService.search(searchFolder, pagination, params.filter?.trim())

		render([searchFolder:searchFolder?.asMap(), folders:results.results*.asMap(), total:results.total] as JSON)
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
		def child = Folder.get(params.long('childId'))
		assert child

		def currentParent = Folder.load(params.long('currentParentId'))
		assert currentParent == child.parent

		folderService.addChildToFolder(parent, child)

		render([notification:notificationService.success('document-vault.api.folder.addFolder.success', [child.name, parent.name])] as JSON)
	}
}
