package us.paperlesstech

import us.paperlesstech.nimble.User

class Note implements Comparable<Note> {
	User user
	DocumentData data
	Date dateCreated
	String note

	static belongsTo = [document:Document]

	static constraints = {
		data nullable:true
		note blank:true, nullable:true, maxSize:4096
	}

	@Override
	int compareTo(Note other) {
		other?.dateCreated <=> dateCreated
	}

	@Override
	String toString() {
		"Note($id)"
	}
}
