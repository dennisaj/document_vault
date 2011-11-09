package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant
import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.User

@MultiTenant
class Folder {
	static def authService
	def grailsApplication

	User createdBy
	Date dateCreated
	Group group
	Date lastUpdated
	User lastUpdatedBy
	String name

	static hasMany = [children:Folder, documents:Document]

	static belongsTo = [parent:Folder]

	static constraints = {
		children nullable:true
		createdBy nullable: true
		lastUpdatedBy nullable: true
		parent nullable:true, validator: { val, obj->
			// If parent is null, return null to indicate valid.
			if (!val) {
				return null
			}

			def check
			check = { p->
				p && (p.parent == obj || check(p?.parent))
			}

			// If parent is a descendant of this object, return an error code.
			// Otherwise return null to indicate valid.
			check(val) ? ['validator.fry'] : null
		}
		documents nullable:true
		group nullable:false
		name blank:false, nullable:false, unique:'parent'
	}

	static mapping = {
		children cascade:'none'
	}

	static transients = ['asMap']

	def asMap() {
		[
			id:id,
			name:name,
			dateCreated:dateCreated,
			data: [
				childrenCount:children?.size(),
				documentCount:documents?.size(),
				mimeType:'folder'
			],
			group:[
				id:group.id,
				name:group.name
			],
			parent:[
				id:parent?.id,
				name:parent?.name
			]
		]
	}

	def beforeInsert() {
		if (!authService) {
			authService = grailsApplication?.mainContext?.getBean(AuthService.class)
		}

		createdBy = authService?.authenticatedUser
		lastUpdatedBy = createdBy
	}

	def beforeUpdate() {
		if (!authService) {
			authService = grailsApplication?.mainContext?.getBean(AuthService.class)
		}

		lastUpdatedBy = authService?.authenticatedUser
	}
}