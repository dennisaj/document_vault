package us.paperlesstech

import us.paperlesstech.nimble.Group
import grails.plugin.spock.IntegrationSpec

class BucketServiceIntegrationSpec extends IntegrationSpec {
	BucketService service
	AuthService authService = Mock()

	Bucket bucket1
	Bucket bucket2
	Document document1
	Document document2
	Document document3
	DocumentData dd
	Group group
	Folder folder1
	Folder folder2
	Folder folder3

	def setup() {
		service = new BucketService()
		service.authService = authService

		dd = new DocumentData(mimeType:MimeType.PNG, fileSize:1, fileKey:'1234abc')
		dd.save(flush:true)
		group = new Group(name:'group')
		bucket1 = new Bucket(name:'bucket1', group:group)
		bucket2 = new Bucket(name:'bucket2', group:group)
		group.save(flush:true)
		bucket1.save(flush:true)
		bucket2.save(flush:true)

		folder1 = new Folder(name:'folder1', group:group, bucket:bucket1)
		folder2 = new Folder(name:'folder2', group:group, bucket:bucket1)
		folder3 = new Folder(name:'folder3', group:group)

		document1 = new Document(group:group)
		document1.addToFiles(dd)
		document2 = new Document(group:group)
		document2.addToFiles(dd)
		document3 = new Document(group:group)
		document3.addToFiles(dd)

		folder1.addToDocuments(document1)
		folder2.addToDocuments(document2)
		folder3.addToDocuments(document3)

		bucket1.addToFolders(folder1)
		bucket1.addToFolders(folder2)

		folder1.save(flush:true)
		folder2.save(flush:true)
		folder3.save(flush:true)
		bucket1.save(flush:true)
	}

	def "deleteBucket should remove all of its folders before deleting the bucket"() {
		given:
		1 * authService.canDelete(bucket1) >> true
		def bucketId = bucket1.id
		when:
		service.deleteBucket(bucket1)
		then:
		!Bucket.get(bucketId)
		!Folder.get(folder1.id).bucket
		!Folder.get(folder2.id).bucket
	}

	def "addFolderToBucket should move the folder from one bucket to another"() {
		given:
		1 * authService.canMoveInTo(bucket2) >> true
		1 * authService.canMoveOutOf(folder1.bucket) >> true
		when:
		def outFolder = service.addFolderToBucket(bucket2, folder1)
		then:
		outFolder.id == folder1.id
		!bucket1.folders.contains(outFolder)
		outFolder.bucket == bucket2
		bucket2.folders.contains(outFolder)
	}

	def "addFolderToBucket should work if the folder is not in a current bucket"() {
		given:
		1 * authService.canMoveInTo(bucket2) >> true
		0 * authService.canMoveOutOf(_)
		when:
		def outFolder = service.addFolderToBucket(bucket2, folder3)
		then:
		outFolder.id == folder3.id
		outFolder.bucket == bucket2
		bucket2.folders.contains(outFolder)
	}

	def "removeFolderFromBucket should set the folder's bucket to null"() {
		given:
		1 * authService.canMoveOutOf(folder1.bucket) >> true
		when:
		def outFolder = service.removeFolderFromBucket(folder1)
		then:
		outFolder.id == folder1.id
		!bucket1.folders.contains(outFolder)
		!outFolder.bucket
	}
}
