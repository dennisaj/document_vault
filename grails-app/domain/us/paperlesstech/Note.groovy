package us.paperlesstech

import us.paperlesstech.nimble.User
import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

@MultiTenant
class Note implements Comparable<Note> {
	DocumentData data
	Date dateCreated
	int left = 0
	String note
	int pageNumber = 0
	int top = 0
	User user

	static belongsTo = [document:Document]

	static constraints = {
		data nullable:true
		left min:0
		note blank:true, nullable:true, maxSize:4096
		pageNumber min:0
		top min:0
	}

	static mapping = {
		tenantId index: 'note_tenant_id_idx'

		left column: "_left"
		top column: "_top"
	}

	@Override
	int compareTo(Note other) {
		dateCreated <=> other?.dateCreated
	}

	@Override
	String toString() {
		"Note($id)"
	}

	def asMap() {
		[
			id:id,
			dateCreated:dateCreated,
			note:note,
			pageNumber:pageNumber,
			left:left,
			top:top,
			user:user.asMap()
		]
	}
}
