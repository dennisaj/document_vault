package us.paperlesstech

import grails.converters.JSON
import grails.plugin.spock.ControllerSpec
import spock.lang.Shared
import us.paperlesstech.helpers.NotificationStatus
import us.paperlesstech.nimble.Group

class BucketControllerSpec extends ControllerSpec {
	BucketService bucketService = Mock()

	@Shared
	def group1 = new Group(id:1, name:'group1')
	def group2 = new Group(id:2, name:'group2')
	def bucket1 = new Bucket(id:1, name:'bucket1')
	def bucket2 = new Bucket(id:2, name:'bucket2')
	def bucket3 = new Bucket(id:3, name:'bucket3')
	def folder1 = new Folder(id:1, name:'folder1')

	def setup() {
		controller.bucketService = bucketService
		controller.metaClass.message = { LinkedHashMap arg1 -> 'this is stupid' }

		mockDomain(Group, [group1, group2])
		mockDomain(Bucket, [bucket1, bucket2, bucket3])
		mockDomain(Folder, [folder1])

		bucket1.group = group1
		bucket2.group = group1
		bucket3.group = group2

		folder1.group = group1
		bucket1.addToFolders(folder1)
		folder1.bucket = bucket1
	}

	def "create should throw an AssertError when not given a valid group"() {
		given:
		controller.params.groupId = groupId
		when:
		controller.create()
		then:
		thrown(AssertionError)
		where:
		groupId << ['9', null]
	}

	def "delete should throw an AssertError when given an invalid bucket"() {
		given:
		controller.params.bucketId = bucketId
		when:
		controller.delete()
		then:
		0 * bucketService.deleteBucket(_)
		thrown(AssertionError)
		where:
		bucketId << [null, '123']
	}

	def "delete should call deleteBucket and return a JSON object with a notification entry"() {
		given:
		controller.params.bucketId = '1'
		when:
		controller.delete()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		1 * bucketService.deleteBucket(bucket1)
		results.notification.status == NotificationStatus.Success.name()
	}

	def "create should render errors return by createBucket"() {
		given:
		controller.params.groupId = '1'
		controller.params.name = '   new bucket   '
		when:
		controller.create()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		1 * bucketService.createBucket(group1, 'new bucket') >> { group, name->
			def bucket = new Bucket(group:group, name:name)
			bucket.errors.rejectValue('name', 'this.is.an.error.code.for.name')
			bucket
		}
		results.notification.status == NotificationStatus.Error.name()
		results.validation.name.errors
		!results.validation.name.valid
		!results.validation.group.errors
		results.validation.group.valid
	}

	def "create should render the saved bucket when there are no errors"() {
		controller.params.groupId = '1'
		controller.params.name = '   new bucket2   '
		when:
		controller.create()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		1 * bucketService.createBucket(group1, 'new bucket2') >> { group, name->
			new Bucket(id:4, group:group, name:name)
		}
		results.notification.status == NotificationStatus.Success.name()
		results.bucket.id == 4
		results.bucket.name == 'new bucket2'
		results.bucket.group.id == group1.id
		results.bucket.group.name == group1.name
	}

	def "list should pass group and filter to search then return the results as JSON"() {
		given:
		controller.params.groupId = groupId
		controller.params.filter = filter
		when:
		controller.list()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		1 * bucketService.search(group, filter) >> [(group1):[bucket1, bucket2], (group2):bucket3]
		results.groups[0].id == group1.id
		results.groups[0].name == group1.name
		results.groups[0].buckets.size() == 2
		results.groups[1].id == group2.id
		results.groups[1].name == group2.name
		results.groups[1].buckets.size() == 1
		where:
		filter << [null, 'filter']
		groupId << [null, '1']
		group << [null, group1]
	}

	def "addFolder should throw an AssertionError when passed invalid data"() {
		given:
		controller.params.bucketId = bucketId
		controller.params.folderId = folderId
		controller.params.currentBucketId = currentBucketId
		when:
		controller.addFolder()
		then:
		0 * bucketService.addFolderToBucket(_, _)
		thrown(AssertionError)
		where:
		bucketId | folderId | currentBucketId
		'9'      | '1'      | '1'             // Bad bucketId
		null     | '1'      | '1'             // Bad bucketId
		'2'      | '2'      | '1'             // Bad folderId
		'2'      | null     | '1'             // Bad folderId
		'2'      | '1'      | '2'             // Incorrect currentBucketId
		'2'      | '1'      | null            // Incorrect currentBucketId
	}

	def "addFolder should addFolderToBucket when given valid data"() {
		given:
		controller.params.bucketId = '2'
		controller.params.folderId = '1'
		controller.params.currentBucketId = '1'
		when:
		controller.addFolder()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		1 * bucketService.addFolderToBucket(bucket2, folder1)
		results.notification.status == NotificationStatus.Success.name()
	}

	def "removeFolder should throw an AssertionError when passed invalid data"() {
		given:
		controller.params.bucketId = bucketId
		controller.params.folderId = folderId
		when:
		controller.removeFolder()
		then:
		0 * bucketService.removeFolderFromBucket(_)
		thrown(AssertionError)
		where:
		bucketId | folderId
		'9'      | '1'      // Bad bucketId
		null     | '1'      // Bad bucketId
		'2'      | '2'      // Bad folderId
		'2'      | null     // Bad folderId
		'2'      | '1'      // Bad current bucket
	}

	def "removeFolder should addFolderToBucket when given valid data"() {
		given:
		controller.params.bucketId = '1'
		controller.params.folderId = '1'
		when:
		controller.removeFolder()
		def results = JSON.parse(mockResponse.contentAsString)
		then:
		1 * bucketService.removeFolderFromBucket(folder1)
		results.notification.status == NotificationStatus.Success.name()
	}
}
