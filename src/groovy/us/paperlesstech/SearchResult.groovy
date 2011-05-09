package us.paperlesstech

class SearchResult {
	int max
	int offset
	List results
	int total

	String toString() {
		this.properties.collect {
			if (it.key == "metaClass" || it.key == "class") {
				return
			}

			"$it.key: $it.value"
		}.join(", ")
	}
}
