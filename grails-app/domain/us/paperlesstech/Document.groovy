package us.paperlesstech

import org.grails.taggable.Taggable

class Document implements Taggable {
	static transients = ["previewImage", "previewImageAsMap", "signed"]
	Date dateCreated
	SortedSet files
	String name
	Map otherFields = [:]
	SortedSet previewImages
	Map searchFields = [:]

	static hasMany = [previewImages: PreviewImage, files: DocumentData]

	static constraints = {
		// minSize won't fire on the initial save if files is null
		files(nullable: false, minSize: 1)
		name(nullable: true, blank: true)
	}

	static mapping = {
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

	// TODO fix me
	def signed = {
		false
	}

	String toString() {
		name ?: "Document(${id})"
	}
}
