package us.paperlesstech

import grails.plugin.multitenant.core.annotation.MultiTenant
import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.User
import org.grails.taggable.Taggable

@MultiTenant
class Folder implements Taggable {
	static def authService
	def grailsApplication
	def tenantService

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
		parent nullable:true, validator: { proposedParentValue, domainInstance ->
			// If parent is null, return null to indicate valid.
			if (!proposedParentValue) {
				return null
			}

			def checkChildrenForFolder = null
			checkChildrenForFolder = { Folder folder ->
				if (folder == proposedParentValue) {
					return true
				}

				// Recursively check if any of this folder's children (if present) are the proposed parent
				return folder.children.any(checkChildrenForFolder)
			}

			// If parent is a descendant of this object, return an error code.
			// Otherwise return null to indicate valid.
			checkChildrenForFolder(domainInstance) ? ['validator.fry'] : null
		}
		documents nullable:true
		group nullable:false
		name blank:false, nullable:false, unique:'parent'
	}

	static mapping = {
		tenantId index: 'folder_tenant_id_idx'

		children cascade:'none'
	}

	static transients = ['asMap']

	def asMap() {
		if (!authService) {
			authService = grailsApplication?.mainContext?.getBean(AuthService.class)
		}

		def map = [
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
			flags: this.flags,
			parent:[
				id:parent?.id,
				name:parent?.name
			]
		]

		map.permissions = [:]
		map.permissions.manage = authService.canManageFolders(this.group)

		map
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

	List<String> getFlags() {
		def t = this.tags
		t ? t.intersect(tenantService?.getTenantConfigList('flag')) : []
	}
}
