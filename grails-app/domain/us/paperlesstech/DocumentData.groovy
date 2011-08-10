package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

import java.util.Date

@MultiTenant
class DocumentData implements Cloneable, Comparable {
	Date dateCreated
	String fileKey
	int fileSize
	MimeType mimeType
	int pages = 1

	static constraints = {
		fileKey(blank: false, nullable: false, unique: true)
		fileSize(min: 1)
		mimeType(blank: false, nullable: false)
		pages(min: 1, max: 10000)
	}

	static mapping = {
		cache 'read-only'
	}

	@Override protected Object clone() {
		new DocumentData(this)
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
