package us.paperlesstech

import grails.plugin.spock.UnitSpec
import us.paperlesstech.nimble.Group

class BucketServiceSpec extends UnitSpec {
	AuthService authService = Mock()
	BucketService service

	Bucket bucket1 = new Bucket(id:1, name:'bucket1')
	Bucket bucket2 = new Bucket(id:2, name:'bucket2')
	Group group1 = new Group(id:1, name:'group1')
	Group group2 = new Group(id:2, name:'group2')
	Folder folder1 = new Folder(id:1)
	Folder folder2 = new Folder(id:2)
	Folder folder3 = new Folder(id:3)
	Folder folder4 = new Folder(id:4)

	def setup() {
		mockLogging(BucketService)
		service = new BucketService()
		service.authService = authService

		bucket1.group = group1
		bucket2.group = group1
		folder1.group = group1
		folder2.group = group1
		folder3.group = group2
		folder4.group = group1

		folder1.bucket = bucket1
		folder2.bucket = bucket1

		mockDomain(Bucket, [bucket1, bucket2])
		mockDomain(Folder, [folder1, folder2, folder3, folder4])
		mockDomain(Group, [group1, group2])

		bucket1.addToFolders(folder1)
		bucket1.addToFolders(folder2)
	}

	def "createBucket should require group"() {
		when:
		service.createBucket(null, 'name')
		then:
		thrown(AssertionError)
	}

	def "createBucket should throw an Assertion error when the user lacks the correct permissions"() {
		given:
		1 * authService.canCreateBucket(group1) >> false
		when:
		def bucket = service.createBucket(group1, 'name')
		then:
		thrown(AssertionError)
	}

	def "createBucket should return errors if validation fails"() {
		given:
		1 * authService.canCreateBucket(group1) >> true
		when:
		def savedBucket = service.createBucket(group1, '')
		then:
		savedBucket.errors
	}

	def "createBucket should return a Bucket if it succeeds"() {
		given:
		1 * authService.canCreateBucket(group1) >> true
		when:
		def savedBucket = service.createBucket(group1, 'bucket!')
		then:
		savedBucket.id
	}

	def "deleteBucket should require a bucket"() {
		when:
		service.deleteBucket(null)
		then:
		thrown(AssertionError)
	}

	def "deleteBucket should throw an Assertion error when the user lacks the correct permissions"() {
		given:
		1 * authService.canDelete(bucket1) >> false
		when:
		service.deleteBucket(bucket1)
		then:
		thrown(AssertionError)
	}

	def "addFolderToBucket should require a bucket"() {
		when:
		service.addFolderToBucket(null, folder1)
		then:
		thrown(AssertionError)
	}

	def "addFolderToBucket should require a folder"() {
		when:
		service.addFolderToBucket(bucket1, null)
		then:
		thrown(AssertionError)
	}

	def "addFolderToBucket should require the folder to be in the same group as the destination bucket"() {
		given:
		folder3.group = group2
		when:
		service.addFolderToBucket(bucket2, folder3)
		then:
		thrown(AssertionError)
	}

	def "addFolderToBucket should throw an Assertion error when the user lacks the canMoveInTo permission"() {
		given:
		1 * authService.canMoveInTo(bucket2) >> false
		when:
		service.addFolderToBucket(bucket2, folder1)
		then:
		thrown(AssertionError)
	}

	def "addFolderToBucket should return if the destination bucket is the same as the current bucket"() {
		given:
		0 * authService.canMoveInTo(_)
		when:
		def outFolder = service.addFolderToBucket(folder1.bucket, folder1)
		then:
		outFolder.is folder1
	}

	def "addFolderToBucket should not check the canMoveOutOf permission if the folder has no bucket"() {
		given:
		1 * authService.canMoveInTo(bucket2) >> true
		when:
		service.addFolderToBucket(bucket2, folder4)
		then:
		0 * authService.canMoveOutOf(_)
	}

	def "addFolderToBucket should check the canMoveOutOf permission if the folder has a bucket and throw an AssertError if the user is denied"() {
		given:
		1 * authService.canMoveInTo(bucket2) >> true
		1 * authService.canMoveOutOf(folder1.bucket) >> false
		when:
		service.addFolderToBucket(bucket2, folder1)
		then:
		thrown(AssertionError)
	}

	def "addFolderToBucket should check the canMoveOutOf permission if the folder has a bucket and continue if the user is permitted"() {
		given:
		1 * authService.canMoveInTo(bucket2) >> true
		when:
		service.addFolderToBucket(bucket2, folder1)
		then:
		1 * authService.canMoveOutOf(folder1.bucket) >> true
	}

	def "removeFolderFromBucket should require a folder"() {
		when:
		service.removeFolderFromBucket(null)
		then:
		thrown(AssertionError)
	}

	def "removeFolderFromBucket should not check the canMoveOutOf permission if the folder has no bucket"() {
		when:
		service.removeFolderFromBucket(folder3)
		then:
		0 * authService.canMoveOutOf(_)
	}

	def "removeFolderFromBucket should check the canMoveOutOf permission if the folder has a bucket and throw an AssertError if the user is denied"() {
		given:
		1 * authService.canMoveOutOf(folder1.bucket) >> false
		when:
		service.removeFolderFromBucket(folder1)
		then:
		thrown(AssertionError)
	}

	def "removeFolderFromBucket should check the canMoveOutOf permission if the folder has a bucket and continue if the user is permitted"() {
		when:
		service.removeFolderFromBucket(folder1)
		then:
		1 * authService.canMoveOutOf(folder1.bucket) >> true
	}
}
