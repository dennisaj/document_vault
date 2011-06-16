package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

import java.util.Date
import java.util.List

@MultiTenant
class Party {
	static final List allowedPermissions = [DocumentPermission.Sign, DocumentPermission.View]
	static transients = ["pageHighlights", "resetHighlights"]

	String code
	PartyColor color
	Date dateCreated
	DocumentPermission documentPermission
	Date expiration
	boolean sent = false
	User signator

	public Party() {
		code = UUID.randomUUID().toString()
	}

	static belongsTo = [document:Document]

	static hasMany = [highlights:Highlight]

	static constraints = {
		code blank:false, nullable:false, unique:true
		color blank:false, nullable:false
		documentPermission nullable:false, inList:allowedPermissions
		expiration nullable:true, validator: {val, obj->
			if (!val) {
				return true
			}

			def now = new Date()
			now.clearTime()

			(now > val) ? ['validator.pastdate'] : null
		}
		highlights nullable:true
		signator unique:"document"
	}

	def pageHighlights = {pageNumber->
		highlights.findAll { it.pageNumber == pageNumber }.collect { it.toMap() }
	}

	/**
	 * Deletes all of the highlights from this party
	 */
	def resetHighlights = {
		highlights?.each {
			it.delete()
		}

		highlights?.clear()
		// TODO Find a way to remove this save
		save(flush:true)
	}
}
