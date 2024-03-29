package us.paperlesstech

import grails.plugin.multitenant.core.annotation.MultiTenant

@MultiTenant
class Highlight {
	Date accepted
	int height
	int left
	int pageNumber
	boolean required = false
	int width
	int top

	static belongsTo = [party:Party]

	static constraints = {
		accepted nullable:true
		height min:0
		pageNumber min:1, max:10000
		width min:0
	}

	static mapping = {
		tenantId index: 'highlight_tenant_id_idx'

		left column: "_left"
		top column: "_top"
	}

	Map asMap() {
		[
			id:id,
			partyId: party.id,
			pageNumber: pageNumber,
			accepted: accepted,
			height:height,
			left:left,
			width:width,
			top:top
		]
	}
}
