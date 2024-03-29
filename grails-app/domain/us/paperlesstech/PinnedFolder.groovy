package us.paperlesstech

import grails.plugin.multitenant.core.annotation.MultiTenant
import us.paperlesstech.nimble.User

@MultiTenant
class PinnedFolder {
	Folder folder
	User user

	static constraints = {
		folder nullable: false, unique: ['user', 'tenantId']
	}

	static mapping = {
		tenantId index: 'pinned_folder_tenant_id_idx'

		folder index: '_user_to_folder_folder_idx'
		user index: '_user_to_folder_user_idx'
		version false
	}
}
