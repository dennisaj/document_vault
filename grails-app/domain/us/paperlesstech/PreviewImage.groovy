package us.paperlesstech

class PreviewImage implements Cloneable, Comparable {
	static belongsTo = [document: Document]
	static transients = ["imageAsMap"]
	DocumentData data
	Date dateCreated
	int height
	int pageNumber
	int width

	static constraints = {
		data(nullable: false)
		pageNumber(unique: "document", min: 1, max: 10000)
	}

	static mapping = {
		data(nullable: false, lazy: true, cascade: "persist, merge, save-update, lock, refresh, evict")
	}

	@Override protected Object clone() {
		new PreviewImage(data: data.clone(), height: height, pageNumber: pageNumber, width: width)
	}

	public int compareTo(def other) {
		return pageNumber <=> other?.pageNumber
	}

	public Map getImageAsMap() {
		[pageNumber: pageNumber, sourceHeight: height, sourceWidth: width]
	}

	String toString() {
		"PreviewImage(${id})"
	}
}