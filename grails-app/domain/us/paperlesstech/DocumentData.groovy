package us.paperlesstech

class DocumentData implements Cloneable, Comparable {
	byte[] data
	Date dateCreated
	MimeType mimeType
	int pages = 1

	static constraints = {
		data(maxSize: 20 * 1024 * 1024)
		mimeType(blank: false, nullable: false)
		pages(min: 1, max: 10000)
	}

	static mapping = {
		data lazy: true
		cache 'read-only'
	}

	@Override protected Object clone() {
		new DocumentData(mimeType: mimeType, pages: pages, data: data.clone())
	}

	@Override
	public int compareTo(def other) {
		return other?.dateCreated <=> dateCreated
	}

	@Override
	String toString() {
		"DocumentData(${id})"
	}
}
