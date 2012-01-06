package us.paperlesstech

import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import spock.lang.Specification

import us.paperlesstech.handlers.Handler
import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.User

@TestFor(DocumentController)
@Mock([Group, Document, DocumentData, PreviewImage, Folder, User, Party])
class DocumentControllerSpec extends Specification {
	AuthService authService = Mock()
	DocumentService documentService = Mock()
	Handler handlerChain = Mock()
	NotificationService notificationService = Mock()
	TenantService tenantService = Mock()

	def group1 = new Group(id:1, name:'group1')
	def documentData = new DocumentData(id:1, pages:4, mimeType:MimeType.PNG, fileSize:123)
	def previewImage = new PreviewImage(id:1, pageNumber:1, sourceHeight:100, sourceWidth:100, data:documentData, thumbnail:documentData)
	def document1 = new Document(id:1, name:'document1', group:group1, files:([documentData] as SortedSet), previewImages:([previewImage] as SortedSet))
	def document2 = new Document(id:2, name:'document2', group:group1, files:([documentData] as SortedSet), previewImages:([previewImage] as SortedSet))
	def folder1 = new Folder(id:1, name:'folder1', group:group1)

	def setup() {
		controller.authService = authService
		controller.documentService = documentService
		controller.handlerChain = handlerChain
		controller.notificationService = notificationService
		controller.tenantService = tenantService

		Document.authService = authService
		Folder.authService = authService

		folder1.save(failOnError:true)
		document1.save(failOnError:true)
		document2.save(failOnError:true)

		Document.metaClass.getTags = { -> null }
		Folder.metaClass.getTags = { -> null }
	}

	def cleanup() {
		Document.metaClass.getTags = null
		Folder.metaClass.getTags = null
	}

	def "downloadImage should return a 404 when given invalid documentId"() {
		when:
		controller.downloadImage(documentId, null)
		then:
		response.status == 404
		where:
		documentId << [null, 100L]
	}

	def "downloadImage should call handlerChain's downloadPreview when given valid input"() {
		given:
		controller.metaClass.cache = { LinkedHashMap arg1 -> 'this is stupid' }
		when:
		controller.downloadImage(1L, previewImage.data.id)
		then:
		1 * handlerChain.downloadPreview([document:document1, previewImage: previewImage]) >> { LinkedHashMap-> ['filename', new ByteArrayInputStream([1] as byte[]), MimeType.PNG.downloadContentType, 1] }
		response.status == 200
		where:
		pageNumber << [null, 1]
	}

	def "download should return a 404 when given invalid documentId or documentDataId"() {
		given:
		controller.params.documentId = documentId
		controller.params.documentDataId = documentDataId
		when:
		controller.download()
		then:
		response.status == 404
		where:
		documentId << [null, 1l]
		documentDataId << [3L, 3L]
	}

	def "download should call handlerChain's download when given valid input"() {
		given:
		controller.metaClass.cache = { LinkedHashMap arg1 -> 'this is stupid' }
		when:
		controller.download(2L, 1L)
		then:
		1 * handlerChain.download([document:document2, documentData:documentData]) >> { LinkedHashMap-> ['filename', new ByteArrayInputStream([1] as byte[]), MimeType.PNG.downloadContentType, 1] }
		response.status == 200
	}

	def "thumbnail should return a 404 when given invalid documentId or documentDataId"() {
		when:
		controller.thumbnail(documentId, documentDataId, 1)
		then:
		response.status == 404
		where:
		documentId << [null, 1L]
		documentDataId << [3L, 3L]
	}

	def "thumbnail should call handlerChain's downloadThumbnail when given valid input"() {
		given:
		controller.metaClass.cache = { LinkedHashMap arg1 -> 'this is stupid' }
		when:
		controller.thumbnail(2L, 1L, pageNumber)
		then:
		1 * handlerChain.downloadThumbnail([document:document2, page:1]) >> { LinkedHashMap-> ['filename', new ByteArrayInputStream([1] as byte[]), MimeType.PNG.downloadContentType, 1] }
		response.status == 200
		where:
		pageNumber << [null, 1]
	}

	def "show should throw an AssertionError when given an invalid documentId"() {
		when:
		controller.show(null)
		then:
		thrown(AssertionError)
	}

	def "show should return the document for the given documentId"() {
		given:
		controller.params.documentId = '1'
		document1.metaClass.previewImageAsMap = { int pageNumber-> [:] }
		when:
		controller.show(1L)
		def documentJSON = JSON.parse(response.contentAsString)
		then:
		documentJSON.document.id == document1.id
	}

	def "image should throw an AssertionError when given an invalid documentId"() {
		when:
		controller.image(null, null)
		then:
		thrown(AssertionError)
	}

	def "image should return a map representing a previewImage when given valid input"() {
		when:
		controller.image(1L, pageNumber)
		def previewImageMap = JSON.parse(response.contentAsString)
		then:
		previewImageMap.pageNumber == pageNumber
		!previewImageMap.savedHighlights
		where:
		pageNumber = 1
	}

	def "image should include savedHighlights when the user canSign or canGetSigned"() {
		given:
		document1.metaClass.highlightsAsMap = { int pageNumber -> [1:'highlight'] }
		when:
		controller.image(1L, 1)
		def previewImageMap = JSON.parse(response.contentAsString)
		then:
		previewImageMap.pageNumber == pageNumber
		previewImageMap.savedHighlights
		1 * authService.canGetSigned(document1) >> canGetSigned
		authService.canSign(document1) >> canSign
		where:
		pageNumber = 1
		canGetSigned << [true, false]
		canSign << [false, true]
	}

	def "list should pass folder, pagination and filter to search then return the results as JSON"() {
		when:
		controller.list(folderId, filter, pagination.max, pagination.sort, pagination.order, pagination.offset)
		def results = JSON.parse(response.contentAsString)
		then:
		1 * documentService.filter(Folder.get(folderId), _, filter) >> [results:[document1, document2], total:2]
		results.documents[0].id == document1.id
		results.documents[0].name == document1.name
		results.documents[1].id == document2.id
		results.documents[1].name == document2.name
		results.documentTotal == 2
		where:
		filter << [null, 'filter']
		folderId << [null, 1L]
		pagination << [[:], [sort:'name', order:'asc', max:10, offset:0]]
	}

	def 'flag requires the documentId'() {
		when:
		controller.flag(null, 'Flag')
		then:
		thrown(AssertionError)
	}

	def 'flag requires the flag name'() {
		when:
		controller.flag(document1.id, null)
		then:
		thrown(AssertionError)
	}

	def 'flag requires the flag is a tenant flag'() {
		when:
		controller.flag(document1.id, 'notaflag')
		then:
		1 * tenantService.getTenantConfigList('flag') >> ['Waiting']
		thrown(AssertionError)
	}

	def 'flag requires the user can flag the document'() {
		when:
		controller.flag(document1.id, 'Waiting')
		then:
		1 * tenantService.getTenantConfigList('flag') >> ['Waiting']
		1 * authService.canFlag(document1) >> false
		thrown(AssertionError)
	}

	def 'flag returns success after tagging the document'() {
		given:
		def tagAdded = null
		document1.metaClass.addTag = { String tag -> tagAdded = tag }
		when:
		controller.flag(document1.id, 'Waiting')
		def results = JSON.parse(response.contentAsString)
		then:
		tagAdded == 'Waiting'
		1 * tenantService.getTenantConfigList('flag') >> ['Waiting']
		1 * authService.canFlag(document1) >> true
		1 * notificationService.success(_, _)
	}

	def 'unflag requires the documentId'() {
		when:
		controller.unflag(null, 'Flag')
		then:
		thrown(AssertionError)
	}

	def 'unflag requires the flag name'() {
		when:
		controller.unflag(document1.id, null)
		then:
		thrown(AssertionError)
	}

	def 'unflag requires the flag is a tenant flag'() {
		when:
		controller.unflag(document1.id, 'notaflag')
		then:
		1 * tenantService.getTenantConfigList('flag') >> ['Waiting']
		thrown(AssertionError)
	}

	def 'unflag requires the user can manager documents'() {
		when:
		controller.unflag(document1.id, 'Waiting')
		then:
		1 * tenantService.getTenantConfigList('flag') >> ['Waiting']
		1 * authService.canFlag(document1) >> false
		thrown(AssertionError)
	}

	def 'unflag returns success if the document did not have the tag'() {
		given:
		document1.metaClass.getTags = { -> [] }
		when:
		controller.unflag(document1.id, 'Waiting')
		def results = JSON.parse(response.contentAsString)
		then:
		1 * tenantService.getTenantConfigList('flag') >> ['Waiting']
		1 * authService.canFlag(document1) >> true
		1 * notificationService.success(_, _)
	}

	def 'unflag returns success after removing the tag from the document'() {
		def tagRemoved = null
		given:
		document1.metaClass.getTags = { -> ['Waiting'] }
		document1.metaClass.removeTag = { String tag -> tagRemoved = tag }
		document1.metaClass.getFlags = { -> [] }
		when:
		controller.unflag(document1.id, 'Waiting')
		def results = JSON.parse(response.contentAsString)
		then:
		tagRemoved == 'Waiting'
		1 * tenantService.getTenantConfigList('flag') >> ['Waiting']
		1 * authService.canFlag(document1) >> true
		1 * notificationService.success(_, _)
	}
}
