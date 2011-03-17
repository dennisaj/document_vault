package us.paperlesstech

class Role {
	String name

	static mapping = {
		cache true
	}

	static constraints = {
		name(blank: false, unique: true)
	}

	String toString() {
		"Role(${id}) - ${name}"
	}
}
