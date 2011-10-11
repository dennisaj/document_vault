package us.paperlesstech

import us.paperlesstech.nimble.Group
import grails.plugin.spock.IntegrationSpec

class BucketServiceIntegrationSpec extends IntegrationSpec {
	BucketService service
	AuthService authService = Mock()

	Bucket bucket1
	Bucket bucket2
	Bucket bucket3
	Document document1
	Document document2
	Document document3
	DocumentData dd
	Group group1
	Group group2
	Folder folder1
	Folder folder2
	Folder folder3

	def setup() {
		service = new BucketService()
		service.authService = authService

		dd = new DocumentData(mimeType:MimeType.PNG, fileSize:1, fileKey:'1234abc')
		dd.save(failOnError:true)
		group1 = new Group(name:'group1')
		group2 = new Group(name:'group2')
		bucket1 = new Bucket(name:'bucket1', group:group1)
		bucket2 = new Bucket(name:'bucket2', group:group1)
		bucket3 = new Bucket(name:'bucket3', group:group2)
		group1.save(failOnError:true)
		group2.save(failOnError:true)
		bucket1.save(failOnError:true)
		bucket2.save(failOnError:true)
		bucket3.save(failOnError:true)

		folder1 = new Folder(name:'folder1', group:group1, bucket:bucket1)
		folder2 = new Folder(name:'folder2', group:group1, bucket:bucket1)
		folder3 = new Folder(name:'folder3', group:group1)

		document1 = new Document(group:group1)
		document1.addToFiles(dd)
		document2 = new Document(group:group1)
		document2.addToFiles(dd)
		document3 = new Document(group:group1)
		document3.addToFiles(dd)

		folder1.addToDocuments(document1)
		folder2.addToDocuments(document2)
		folder3.addToDocuments(document3)

		bucket1.addToFolders(folder1)
		bucket1.addToFolders(folder2)

		folder1.save(failOnError:true)
		folder2.save(failOnError:true)
		folder3.save(failOnError:true)
		bucket1.save(failOnError:true)
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

	def "search should return all available buckets when not given a group"() {
		when:
		def result = service.search()
		then:
		1 * authService.getIndividualBucketsWithPermission(BucketPermission.values() as List) >> ([] as Set)
		1 * authService.getGroupsWithPermission(BucketPermission.values() as List) >> ([group1, group2] as SortedSet)
		result.values()*.size().sum() == 3
		result[(group1)].contains(bucket1)
		result[(group1)].contains(bucket2)
		result[(group2)].contains(bucket3)
	}

	def "search should return only return buckets from the given group"() {
		when:
		def result = service.search(group1)
		then:
		1 * authService.getIndividualBucketsWithPermission(BucketPermission.values() as List) >> ([] as Set)
		1 * authService.getGroupsWithPermission(BucketPermission.values() as List) >> ([group1, group2] as SortedSet)
		result.values()*.size().sum() == 2
		result[(group1)].contains(bucket1)
		result[(group1)].contains(bucket2)
		!result[(group2)]
	}
}
