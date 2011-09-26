package us.paperlesstech

import grails.converters.JSON
import grails.plugin.spock.ControllerSpec
import us.paperlesstech.handlers.Handler

class DocumentControllerSpec extends ControllerSpec {
	AuthService authService = Mock()
	DocumentService documentService = Mock()
	Handler handlerChain = Mock()

	def documentData = new DocumentData(id:1, pages:4, dateCreated: new Date())
	def previewImage = new PreviewImage(id:1, pageNumber:1, sourceHeight:100, sourceWidth:100, data:documentData, thumbnail:documentData)
	def document1 = new Document(id:1, files:([documentData] as SortedSet), previewImages:([previewImage] as SortedSet))
	def document2 = new Document(id:2, files:([documentData] as SortedSet), previewImages:([previewImage] as SortedSet))
	def party = new Party(id:1, document:document1)

	def setup() {
		controller.authService = authService
		controller.documentService = documentService
		controller.handlerChain = handlerChain

		mockDomain(Document, [document1, document2])
		mockDomain(DocumentData, [documentData])
		mockDomain(PreviewImage, [previewImage])
		mockDomain(Party, [party])
	}

	def "index should render a template when called from ajax"() {
		given:
		mockRequest.makeAjaxRequest()
		1 * documentService.search(_) >> { params-> model }
		when:
		controller.index()
		then:
		controller.renderArgs.template == 'searchResults'
		controller.renderArgs.model == model
		where:
		model = [here:'is the model']
	}

	def "index should return a model when not called from ajax"() {
		given:
		1 * documentService.search(_) >> { params-> model }
		when:
		def outModel = controller.index()
		then:
		outModel == model
		where:
		model = [here:'is the model']
	}

	def "downloadImage should return a 404 when given invalid documentId"() {
		given:
		controller.params.documentId = documentId
		when:
		controller.downloadImage()
		then:
		mockResponse.status == 404
		where:
		documentId << [null, 100]
	}

	def "downloadImage should call handlerChain's downloadPreview when given valid input"() {
		given:
		controller.params.documentId = '1'
		controller.params.pageNumber = pageNumber
		1 * handlerChain.downloadPreview([document:document1, page:1]) >> { LinkedHashMap-> ['filename', new ByteArrayInputStream([1] as byte[]), MimeType.PNG.downloadContentType, 1] }
		when:
		controller.downloadImage()
		then:
		mockResponse.status == 200
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
		mockResponse.status == 404
		where:
		documentId << [null, 1]
		documentDataId << [3, 3]
	}

	def "download should call handlerChain's download when given valid input"() {
		given:
		controller.metaClass.cache = { LinkedHashMap arg1 -> 'this is stupid' }
		controller.params.documentId = '2'
		controller.params.documentDataId = '1'
		1 * handlerChain.download([document:document2, documentData:documentData]) >> { LinkedHashMap-> ['filename', new ByteArrayInputStream([1] as byte[]), MimeType.PNG.downloadContentType, 1] }
		when:
		controller.download()
		then:
		mockResponse.status == 200
	}

	def "thumbnail should return a 404 when given invalid documentId or documentDataId"() {
		given:
		controller.params.documentId = documentId
		controller.params.documentDataId = documentDataId
		controller.params.pageNumber = '1'
		when:
		controller.thumbnail()
		then:
		mockResponse.status == 404
		where:
		documentId << [null, 1]
		documentDataId << [3, 3]
	}

	def "thumbnail should call handlerChain's downloadThumbnail when given valid input"() {
		given:
		controller.metaClass.cache = { LinkedHashMap arg1 -> 'this is stupid' }
		controller.params.documentId = '2'
		controller.params.documentDataId = '1'
		controller.params.pageNumber = pageNumber
		1 * handlerChain.downloadThumbnail([document:document2, page:1]) >> { LinkedHashMap-> ['filename', new ByteArrayInputStream([1] as byte[]), MimeType.PNG.downloadContentType, 1] }
		when:
		controller.thumbnail()
		then:
		mockResponse.status == 200
		where:
		pageNumber << [null, 1]
	}

	def "show should throw an AssertionError when given an invalid documentId"() {
		given:
		controller.params.documentId = null
		when:
		controller.show()
		then:
		thrown(AssertionError)
	}

	def "show should return the document for the given documentId"() {
		given:
		controller.params.documentId = '1'
		when:
		def model = controller.show()
		then:
		model.document == document1
	}

	def "image should throw an AssertionError when given an invalid documentId"() {
		given:
		controller.params.documentId = null
		controller.params.pageNumber = null
		when:
		controller.image()
		then:
		thrown(AssertionError)
	}

	def "image should return a map representing a previewImage when given valid input"() {
		given:
		controller.params.documentId = '1'
		controller.params.pageNumber = pageNumber
		when:
		controller.image()
		def previewImageMap = JSON.parse(mockResponse.contentAsString)
		then:
		previewImageMap.pageNumber == pageNumber
		!previewImageMap.highlights
		where:
		pageNumber = 1
	}

	def "image should include highlights when the user canSign or canGetSigned"() {
		given:
		document1.metaClass.highlightsAsMap = { int pageNumber -> [1:'highlight'] }
		controller.params.documentId = '1'
		controller.params.pageNumber = 1
		authService.canGetSigned(document1) >> canGetSigned
		authService.canSign(document1) >> canSign
		when:
		controller.image()
		def previewImageMap = JSON.parse(mockResponse.contentAsString)
		then:
		previewImageMap.pageNumber == pageNumber
		previewImageMap.highlights
		where:
		pageNumber = 1
		canGetSigned << [true, false]
		canSign << [false, true]
	}

	def "sign should throw an AssertionError when given an invalid documentId"() {
		given:
		controller.params.documentId = null
		when:
		controller.sign()
		then:
		thrown(AssertionError)
	}

	def "sign should a model when given valid input"() {
		given:
		controller.params.documentId = '1'
		when:
		def model = controller.sign()
		then:
		model.document == document1
		model.colors == PartyColor.values()
		model.permissions == Party.allowedPermissions
		model.parties == [party]
	}
}