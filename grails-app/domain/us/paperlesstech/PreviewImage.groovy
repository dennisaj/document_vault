package us.paperlesstech

import org.joda.time.LocalDateTime
import org.joda.time.contrib.hibernate.PersistentLocalDateTime

class PreviewImage implements Comparable {
	static belongsTo = [document: Document]
	static imageDataPrefix = "data:image/png;base64,"
	static transients = ["imageAsMap"]
	DocumentData data
	LocalDateTime dateCreated
	int height
	int pageNumber
	int width

	static constraints = {
		data(nullable: false)
		pageNumber(unique: "document", min: 1, max: 10000)
	}

	static mapping = {
		data(nullable: false, lazy: true, cascade: "persist, merge, save-update, lock, refresh,  evict")
		dateCreated(type: PersistentLocalDateTime)
	}

	public int compareTo(def other) {
		return pageNumber <=> other?.pageNumber
	}

	public Map getImageAsMap() {
		[imageData: imageDataPrefix + data.data.encodeBase64().toString(),
				pageNumber: pageNumber,
				sourceHeight: height,
				sourceWidth: width]
	}

	String toString() {
		"PreviewImage(${id})"
	}
}