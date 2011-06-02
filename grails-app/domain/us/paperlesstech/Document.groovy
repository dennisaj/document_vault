package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

import org.grails.taggable.Taggable
import grails.plugins.nimble.core.Group

@MultiTenant
class Document implements Taggable {
	static searchable = {
		only: ["dateCreated", "name", "tags", "searchFields"]
	}
	static transients = ["otherField", "previewImage", "previewImageAsMap", "searchField"]
	Date dateCreated
	SortedSet files
	Group group
	String name
	SortedSet previewImages
	boolean signed = false

	static hasMany = [previewImages: PreviewImage,
			files: DocumentData,
			searchFieldsCollection: DocumentSearchField,
			otherFieldsCollection: DocumentOtherField]

	static constraints = {
		// minSize won't fire on the initial save if files is null
		files(nullable: false, minSize: 1)
		group(nullable: false)
		name(nullable: true, blank: true)
	}

	static mapping = {
		otherFieldsCollection(cascade: "all, all-delete-orphan")
		searchFieldsCollection(cascade: "all, all-delete-orphan")
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
	 * Returns the image data for the given page. The actual image will be base64 encoded.
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
		save(flush:true)
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

	@Override
	String toString() {
		name ?: "Document(${id})"
	}
}
