package us.paperlesstech

import grails.converters.JSON
import us.paperlesstech.helpers.NotificationHelper
import us.paperlesstech.helpers.NotificationStatus
import us.paperlesstech.nimble.Group

class BucketController {
	static def allowedMethods = [create:'POST', list:'GET']

	def bucketService

	def create = {
		def group = Group.load(params.long('groupId'))
		assert group

		def bucket = bucketService.createBucket(group, params.name?.trim())
		def returnMap = [:]

		if (bucket.hasErrors()) {
			returnMap.notification = NotificationHelper.notification('title', 'message', NotificationStatus.Error)

			// TODO replace with collectEntries with Groovy 1.8.0
			returnMap.validation = [:].putAll(['group', 'name'].collect { field->
				new MapEntry(field,
					[
						errors:bucket.errors.getFieldErrors(field).collect {
							g.message(error:it, encodeAs:'HTML')
						},
						valid:!bucket.errors.hasFieldErrors(field)
					]
				)
			})
		} else {
			returnMap.notification = NotificationHelper.notification('title', 'message', NotificationStatus.Success)
			returnMap.bucket = bucket.asMap()
		}

		render(returnMap as JSON)
	}

	def delete = {
		def bucket = Bucket.load(params.long('bucketId'))
		assert bucket

		bucketService.deleteBucket(bucket)

		render([notification:NotificationHelper.notification('title', 'message', NotificationStatus.Success)] as JSON)
	}

	def list = {
		def searchGroup = Group.load(params.long('groupId'))

		def results = bucketService.search(searchGroup, params.filter?.trim())

		def groups = [groups:results.collect { group, buckets->
			[
				id:group.id,
				name:group.name,
				buckets:buckets*.asMap()
			]
		}]

		render(groups as JSON)
	}

	def addFolder = {
		def destination = Bucket.load(params.long('bucketId'))
		assert destination
		def folder = Folder.get(params.long('folderId'))
		assert folder
		assert folder.bucket?.id == params.long('currentBucketId')

		bucketService.addFolderToBucket(destination, folder)

		render([notification:NotificationHelper.notification('title', 'message', NotificationStatus.Success)] as JSON)
	}

	def removeFolder = {
		def bucket = Bucket.load(params.long('bucketId'))
		assert bucket
		def folder = Folder.get(params.long('folderId'))
		assert folder
		assert folder.bucket?.id == bucket.id

		bucketService.removeFolderFromBucket(folder)

		render([notification:NotificationHelper.notification('title', 'message', NotificationStatus.Success)] as JSON)
	}
}
