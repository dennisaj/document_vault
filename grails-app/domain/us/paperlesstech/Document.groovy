package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant
import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.User

@MultiTenant
class Document {
	static def authService
	def grailsApplication

	static transients = ["highlightsAsMap", "otherField", "previewImage", "previewImageAsMap", "searchField", "signed", 'asMap']
	User createdBy
	Date dateCreated
	Date lastUpdated
	User lastUpdatedBy
	SortedSet files
	Folder folder
	Group group
	String name
	SortedSet notes
	SortedSet previewImages

	static belongsTo = [folder:Folder]

	static hasMany = [previewImages:PreviewImage,
			files:DocumentData,
			notes:Note,
			searchFieldsCollection:DocumentSearchField,
			otherFieldsCollection:DocumentOtherField,
			parties:Party]

	static constraints = {
		createdBy nullable: true
		// minSize won't fire on the initial save if files is null
		files nullable:false, minSize:1
		folder nullable:true
		group nullable:false
		lastUpdatedBy nullable: true
		name nullable:true, blank:true
		notes nullable:true
		parties nullable:true
	}

	static mapping = {
		files joinTable: [name: 'document_to_document_data']
		otherFieldsCollection cascade: "all, all-delete-orphan"
		searchFieldsCollection cascade: "all, all-delete-orphan"
	}

	def beforeInsert() {
		if (!authService) {
			authService = grailsApplication?.mainContext?.getBean(AuthService.class)
		}

		createdBy = authService?.authenticatedUser
		lastUpdatedBy = createdBy
	}

	def beforeUpdate() {
		if (!authService) {
			authService = grailsApplication?.mainContext?.getBean(AuthService.class)
		}

		lastUpdatedBy = authService?.authenticatedUser
	}

	/**
	* Returns the highlight data for the given page.
	*
	* @param pageNumber Retrieve the data for this page
	*
	* @return A map of the highlights for all parties on the given page.
	*/
	def highlightsAsMap = { int pageNumber ->
		def m = [:]
		parties?.each{ party->
			m.(party.id) = party.pageHighlights(pageNumber)
		}

		m
	}

	/**
	 * Retrieves the value of the passed other field
	 *
	 * @param key They key to look up
	 * @return The value or null if the key does not exist
	 */
	String otherField(String key) {
		otherFieldsCollection?.find({ it.key == key })?.value
	}

	/**
	 * Sets the given key and value in the map.  Overwrites the value of the existing attribute if key is already used
	 *
	 * @param key The key into the map
	 * @param value The value to store
	 */
	void otherField(String key, String value) {
		def existing = otherFieldsCollection?.find { it.key == key }
		if (existing) {
			existing.value = value
		} else {
			addToOtherFieldsCollection(new DocumentOtherField(key: key, value: value))
		}
	}

	/**
	 * Returns the preview image with the given page number
	 *
	 * @param pageNumber The page number to look for
	 *
	 * @return the preview image with the given page number or null if it is not found
	 */
	def previewImage = {int pageNumber ->
		previewImages.find { it.pageNumber == pageNumber }
	}

	/**
	 * Returns the image data for the given page.
	 *
	 * @param pageNumber Retrieve the data for this page
	 *
	 * @return A map of the data contained on the image for the given page.
	 */
	def previewImageAsMap = {int pageNumber ->
		assert previewImages, "${this} has no images."

		def image = previewImage(pageNumber)
		image?.getImageAsMap()
	}

	/**
	 * Deletes all of the documents from the previewImages collection
	 */
	def resetPreviewImages = {
		previewImages?.each {
			it.delete()
		}

		previewImages?.clear()
		// TODO Find a way to remove this save
		save(flush:true, failOnError: true)
	}

	/**
	 * Retrieves the value of the passed search field
	 *
	 * @param key They key to look up
	 * @return The value or null if the key does not exist
	 */
	String searchField(String key) {
		searchFieldsCollection?.find({ it.key == key })?.value
	}

	/**
	 * Sets the given key and value in the map.  Overwrites the value of the existing attribute if key is already used
	 *
	 * @param key The key into the map
	 * @param value The value to store
	 */
	void searchField(String key, String value) {
		def existing = searchFieldsCollection?.find { it.key == key }
		if (existing) {
			existing.value = value
		} else {
			addToSearchFieldsCollection(new DocumentSearchField(key: key, value: value))
		}
	}

	def signed = {
		def signedList = parties.findAll { it.documentPermission == DocumentPermission.Sign }*.completelySigned()
		signedList && signedList?.every { it }
	}

	def asMap() {
		[
			id:id,
			name:name,
			dateCreated:dateCreated,
			data: [
				id:files.first().id,
				size:files.first().fileSize,
				pages:files.first().pages,
				mimeType:files.first().mimeType.name().toLowerCase()
			],
			thumbnail: previewImage(1).thumbnail.id,
			group: [
				id:group.id,
				name:group.name
			],
			parent: [
				id:folder?.id,
				name:folder?.name
			],
			notes: notes*.asMap(),
			signed: signed()
		]
	}

	@Override
	String toString() {
		name ?: "Document(${id})"
	}
}
