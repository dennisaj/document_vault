package us.paperlesstech

import org.joda.time.LocalDateTime
import org.joda.time.contrib.hibernate.PersistentLocalDateTime

class DocumentData implements Comparable {
	byte[] data
	LocalDateTime dateCreated
	MimeType mimeType
	int pages = 1

	static constraints = {
		data(maxSize: 20 * 1024 * 1024)
		mimeType(blank: false, nullable: false)
		pages(min: 1, max: 10000)
	}

	static mapping = {
		data(lazy: true)
		dateCreated(type:PersistentLocalDateTime)
	}

	public int compareTo(def other) {
		return other?.dateCreated <=> dateCreated
	}

	String toString() {
		"DocumentData(${id})"
	}
}
