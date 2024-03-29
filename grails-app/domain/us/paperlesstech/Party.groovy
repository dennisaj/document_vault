package us.paperlesstech

import grails.plugin.multitenant.core.annotation.MultiTenant
import us.paperlesstech.nimble.User

@MultiTenant
class Party {
	static final List allowedPermissions = [DocumentPermission.Sign, DocumentPermission.View]
	static transients = ["completelySigned", "partiallySigned", "removable", "status", "resetHighlights"]

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

	static mapping = {
		tenantId index: 'party_tenant_id_idx'
	}

	static constraints = {
		code blank:false, nullable:false, unique:true
		color nullable:true
		documentPermission nullable:false, inList:allowedPermissions
		expiration nullable:true, validator: { val, obj->
			if (!val || obj.id) {
				return true
			}

			def now = new Date()
			now.clearTime()

			(now > val) ? ['validator.pastdate'] : null
		}
		highlights nullable: true
		signator unique:"document"
	}

	def completelySigned = {
		highlights && highlights*.accepted?.every {it}
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

	def highlightsMappedByPage() {
		highlights.groupBy { highlight ->
			highlight.pageNumber
		}.collectEntries { pageNumber, highlights ->
			[(pageNumber): highlights*.asMap()]
		}
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

	def asMap() {
		[
			id:id,
			color:color?.name(),
			documentPermission:documentPermission.name(),
			expiration:expiration,
			signator:signator.asMap(),
			status:status(),
			removable:removable(),
			highlights:highlightsMappedByPage()
		]
	}
}
