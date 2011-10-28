package us.paperlesstech

import us.paperlesstech.nimble.Group
import grails.plugin.spock.IntegrationSpec

class DocumentServiceIntegrationSpec extends IntegrationSpec {
	DocumentService service
	AuthService authService = Mock()

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
		service = new DocumentService()
		service.authService = authService
		Document.authService = authService
		Folder.authService = authService

		dd = new DocumentData(mimeType:MimeType.PNG, fileSize:1, fileKey:'1234abc')
		dd.save(failOnError:true)
		group1 = new Group(name:'group1')
		group1.save(failOnError:true)

		folder1 = new Folder(name:'folder1', group:group1)
		folder2 = new Folder(name:'folder2', group:group1)

		document1 = new Document(group:group1, name:'document1')
		document1.addToFiles(dd)
		document2 = new Document(group:group1, name:'document2')
		document2.addToFiles(dd)
		document3 = new Document(group:group1, name:'document3')
		document3.addToFiles(dd)
		document4 = new Document(group:group1, name:'document4')
		document4.addToFiles(dd)
		document4.save(failOnError:true)

		folder1.addToDocuments(document1)
		folder2.addToDocuments(document2)
		folder2.addToDocuments(document3)

		folder1.save(failOnError:true)
		folder2.save(failOnError:true)
	}

	def "search should return documents without a folder when folder is null"() {
		when:
		def result = service.search(null, [sort:'name', order:'asc', max:10, offset:0], '')
		then:
		1 * authService.getIndividualDocumentsWithPermission(documentPerms) >> ([] as Set)
		1 * authService.getGroupsWithPermission(documentPerms) >> ([group1] as SortedSet)
		result.documentResults.size() == 1
		result.documentResults[0].id== document4.id
		result.documentResults[0].name == document4.name
		result.documentTotal == 1
	}

	def "search should limit by folder when one is given"() {
		when:
		def result = service.search(folder2, [sort:'name', order:'asc', max:10, offset:0], '')
		then:
		1 * authService.getIndividualDocumentsWithPermission(documentPerms) >> ([] as Set)
		1 * authService.getGroupsWithPermission(documentPerms) >> ([group1] as SortedSet)
		result.documentResults.size() == 2
		result.documentResults[0].id== document2.id
		result.documentResults[0].name == document2.name
		result.documentResults[1].id== document3.id
		result.documentResults[1].name == document3.name
		result.documentTotal == 2
	}

	def "search should limit by document ids when getIndividualDocumentsWithPermission returns values"() {
		when:
		def result = service.search(document1.folder, [sort:'name', order:'asc', max:10, offset:0], '')
		then:
		1 * authService.getIndividualDocumentsWithPermission(documentPerms) >> ([document1.id] as Set)
		1 * authService.getGroupsWithPermission(documentPerms) >> ([] as SortedSet)
		result.documentResults.size() == 1
		result.documentResults[0].id== document1.id
		result.documentResults[0].name == document1.name
		result.documentTotal == 1
	}

	def "search should apply the filter when it is supplied"() {
		when:
		def result = service.search(document1.folder, [sort:'name', order:'asc', max:10, offset:0], 'document1')
		then:
		1 * authService.getIndividualDocumentsWithPermission(documentPerms) >> ([] as Set)
		1 * authService.getGroupsWithPermission(documentPerms) >> ([group1] as SortedSet)
		result.documentResults.size() == 1
		result.documentResults[0].id== document1.id
		result.documentResults[0].name == document1.name
		result.documentTotal == 1
	}
}
