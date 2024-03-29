package us.paperlesstech

import grails.plugin.multitenant.core.annotation.MultiTenant

@MultiTenant
class PreviewImage implements Cloneable, Comparable {
	static belongsTo = [document: Document]
	static transients = ["imageAsMap"]
	DocumentData data
	Date dateCreated
	int pageNumber
	int sourceHeight
	int sourceWidth
	DocumentData thumbnail

	static constraints = {
		data nullable: false
		pageNumber unique: "document", min: 1, max: 10000
		thumbnail nullable: false
	}

	static mapping = {
		tenantId index: 'preview_image_tenant_id_idx'

		data nullable: false, lazy: true, cascade: "persist, merge, save-update, lock, refresh, evict"
		thumbnail nullable: false, lazy: true, cascade: "persist, merge, save-update, lock, refresh, evict"
	}

	@Override
	protected Object clone() {
		new PreviewImage(data: data.clone(), sourceHeight: sourceHeight, pageNumber: pageNumber, sourceWidth: sourceWidth)
	}

	@Override
	public int compareTo(def other) {
		return pageNumber <=> other?.pageNumber
	}

	public Map getImageAsMap() {
		[pageNumber: pageNumber, documentDataId: data.id, sourceHeight: sourceHeight, sourceWidth: sourceWidth]
	}

	@Override
	String toString() {
		"PreviewImage(${id})"
	}
}