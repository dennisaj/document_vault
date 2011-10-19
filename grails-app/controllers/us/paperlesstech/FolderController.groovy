package us.paperlesstech

import grails.converters.JSON
import us.paperlesstech.helpers.NotificationHelper
import us.paperlesstech.nimble.Group

class FolderController {
	def folderService

	def create = {
		def group = Group.load(params.long('groupId'))
		assert group
		def initialDocument = Document.load(params.long('documentId'))
		assert initialDocument

		def folder = folderService.createFolder(group, params.name?.trim(), initialDocument)
		def returnMap = [:]

		if (folder.hasErrors()) {
			returnMap.notification = NotificationHelper.error('title', 'message')

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
			returnMap.notification = NotificationHelper.success('title', 'message')
			returnMap.folder = folder.asMap()
			returnMap.folder.documents = [
				[
					id:initialDocument.id,
					name:initialDocument.name
				]
			]
		}

		render(returnMap as JSON)
	}

	def delete = {
		def folder = Folder.load(params.long('folderId'))
		assert folder

		folderService.deleteFolder(folder)

		render([notification:NotificationHelper.success('title', 'message')] as JSON)
	}

	def list = {
		def searchBucket = Bucket.load(params.long('bucketId'))

		def pagination = [:]
		def max = params.int('max')
		pagination.max = max in 10..100 ? max : (max > 100 ? 100 : 10)
		pagination.sort = params.sort ?: 'name'
		pagination.order = params.order ?: 'asc'
		pagination.offset = params.int('offset') ?: 0

		def results = folderService.search(searchBucket, pagination, params.filter?.trim())

		render([folders:results.results*.asMap(), total:results.total] as JSON)
	}

	def addDocument = {
		def destination = Folder.load(params.long('folderId'))
		assert destination
		def document = Document.get(params.long('documentId'))
		assert document
		assert document.folder?.id == params.long('currentFolderId')

		folderService.addDocumentToFolder(destination, document)

		render([notification:NotificationHelper.success('title', 'message')] as JSON)
	}

	def removeDocument = {
		def folder = Folder.load(params.long('folderId'))
		assert folder
		def document = Document.get(params.long('documentId'))
		assert document
		assert document.folder?.id == folder.id

		folderService.removeDocumentFromFolder(document)

		render([notification:NotificationHelper.success('title', 'message')] as JSON)
	}
}
