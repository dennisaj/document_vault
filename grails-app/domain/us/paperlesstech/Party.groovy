package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant

import java.util.Date
import java.util.List

@MultiTenant
class Party {
	static final List allowedPermissions = [DocumentPermission.Sign, DocumentPermission.View]
	static transients = ["completelySigned", "pageHighlights", "partiallySigned", "removable", "status", "resetHighlights"]

	String code
	PartyColor color
	Date dateCreated
	DocumentPermission documentPermission
	Date expiration
	boolean sent = false
	boolean rejected = false
	User signator
	boolean viewed = false

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
			if (!val || obj.id) {
				return true
			}

			def now = new Date()
			now.clearTime()

			(now > val) ? ['validator.pastdate'] : null
		}
		highlights nullable: true, validator: {val, obj->
			(!val && obj.documentPermission == DocumentPermission.Sign) ? ['validator.nullwhensign'] : null
		}
		signator unique:"document"
	}

	def completelySigned = {
		highlights && highlights*.accepted?.every {it}
	}

	def pageHighlights = {pageNumber->
		highlights.findAll { it.pageNumber == pageNumber }.collect { it.toMap() }
	}

	def partiallySigned = {
		highlights*.accepted?.any {it} && !highlights*.accepted?.every {it}
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
		save(flush:true, failOnError: true)
	}

	def removable = {
		!rejected && !partiallySigned() && !completelySigned()
	}

	def status = {
		def status = "document-vault.view.party.status.unsaved"

		if (viewed) {
			status = "document-vault.view.party.status.viewed"
		} else if (id && !sent) {
			status = "document-vault.view.party.status.unsent"
		} else if (sent) {
			status = "document-vault.view.party.status.sent"
		}

		if (rejected) {
			status = "document-vault.view.party.status.rejected"
		} else if (partiallySigned()) {
			status = "document-vault.view.party.status.partiallysigned"
		} else if (completelySigned()) {
			status = "document-vault.view.party.status.signed"
		}

		status
	}
}
