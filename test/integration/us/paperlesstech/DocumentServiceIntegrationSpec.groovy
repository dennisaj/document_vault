package us.paperlesstech

import us.paperlesstech.nimble.Group
import grails.test.mixin.TestFor

class DocumentServiceIntegrationSpec extends AbstractMultiTenantIntegrationSpec {
	DocumentService service
	// As of Grails 2.0, it appears that integration tests are wired even if they were mocked at class creation
	// the following mock example will be overwritten with an injected actual object
	// AuthService authService = Mock()
	AuthService authService

	List documentPerms = [DocumentPermission.GetSigned, DocumentPermission.Sign, DocumentPermission.View]

	Group group1
	DocumentData dd
	Document document1
	Document document2
	Document document3
	Document document4
	Folder folder1
	Folder folder2

	def setup() {
		// Mocking has to happen in a test method as of Grails 2.0
		authService = Mock()

		service = new DocumentService()
		service.authService = authService
		Document.authService = authService
		Folder.authService = authService

		dd = new DocumentData(mimeType:MimeType.PNG, fileSize:1, fileKey:'1234abc')
		dd.save(failOnError:true)
		group1 = new Group(name:'group1')
		group1.save(failOnError:true)

		folder1 = new Folder(name:'folder1', group:group1)
		folder1.authService = authService
		folder2 = new Folder(name:'folder2', group:group1)
		folder2.authService = authService

		document1 = new Document(group:group1, name:'document1')
		document1.authService = authService
		document1.addToFiles(dd)
		document2 = new Document(group:group1, name:'document2')
		document2.authService = authService
		document2.addToFiles(dd)
		document3 = new Document(group:group1, name:'document3')
		document3.authService = authService
		document3.addToFiles(dd)
		document4 = new Document(group:group1, name:'document4')
		document4.authService = authService
		document4.addToFiles(dd)
		document4.save(failOnError:true)

		folder1.addToDocuments(document1)
		folder2.addToDocuments(document2)
		folder2.addToDocuments(document3)

		folder1.save(failOnError:true)
		folder2.save(failOnError:true)
	}

	def "filter should return documents without a folder when folder is null"() {
		when:
		def result = service.filter(null, [sort:'name', order:'asc', max:10, offset:0], '')
		then:
		1 * authService.getGroupsWithPermission(documentPerms) >> ([group1] as SortedSet)
		result.results.size() == 1
		result.results[0].id== document4.id
		result.results[0].name == document4.name
		result.total == 1
	}

	def "filter should limit by folder when one is given"() {
		when:
		def result = service.filter(folder2, [sort:'name', order:'asc', max:10, offset:0], '')
		then:
		1 * authService.getGroupsWithPermission(documentPerms) >> ([group1] as SortedSet)
		result.results.size() == 2
		result.results[0].id== document2.id
		result.results[0].name == document2.name
		result.results[1].id== document3.id
		result.results[1].name == document3.name
		result.total == 2
	}

	def "filter should apply the filter when it is supplied"() {
		when:
		def result = service.filter(document1.folder, [sort:'name', order:'asc', max:10, offset:0], 'document1')
		then:
		1 * authService.getGroupsWithPermission(documentPerms) >> ([group1] as SortedSet)
		result.results.size() == 1
		result.results[0].id== document1.id
		result.results[0].name == document1.name
		result.total == 1
	}
}
