package us.paperlesstech

import org.joda.time.LocalDateTime
import org.joda.time.contrib.hibernate.PersistentLocalDateTime
import javax.persistence.Transient

class Document {
	static transients = ["previewImage", "previewImageAsMap", "signed"]
	LocalDateTime dateCreated
	SortedSet files
	Map otherFields = [:]
	SortedSet previewImages
	Map searchFields = [:]

	static hasMany = [previewImages: PreviewImage, files: DocumentData]

	static constraints = {
		// minSize won't fire on the initial save if files is null
		files(nullable: false, minSize: 1)
	}

	static mapping = {
		dateCreated(type: PersistentLocalDateTime)
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

	// TODO fix me
	def signed = {
		false
	}

	String toString() {
		"Document(${id})"
	}
}
