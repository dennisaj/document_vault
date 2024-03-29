package us.paperlesstech

import grails.plugin.multitenant.core.annotation.MultiTenant
import us.paperlesstech.nimble.User

@MultiTenant
class ActivityLog {
	def tenantService

	String action
	Date dateCreated
	User delegate
	Document document
	String ip
	String pageNumber
	String params
	int status
	String uri
	User user
	String userAgent

	static mapping = {
		tenantId index: 'activity_log_tenant_id_idx'

		document index: "activity_log_document_idx"
	}

	static constraints = {
		document nullable: true
		delegate nullable: true
		pageNumber nullable: true, blank: true
		params nullable: true, blank: true, maxSize: 4096
		user nullable: true
		uri maxSize: 4096
	}

	Map asMap() {
		def j = [
				action: this.action,
				dateCreated: this.dateCreated,
				delegate: this.delegate?.id,
				document: this.document?.id,
				userAgent: this.userAgent,
				ip: this.ip,
				user: this.user?.id,
				pageNumber: this.pageNumber,
				params: this.params,
				status: this.status,
				tenant: this.tenantService.currentTenant.mappedTenantId,
				uri: this.uri
		]
	}

	void setParams(String params = '') {
		if (params) {
			this.@params = params.substring(0, Math.min(params.length(), 4096 - 1))
		}
	}

	void setUri(String uri) {
		if (uri) {
			this.@uri = uri.substring(0, Math.min(uri.length(), 4096 - 1))
		}
	}
}
