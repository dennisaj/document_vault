package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant
import us.paperlesstech.nimble.Group

@MultiTenant
class Folder {
	Date dateCreated
	Group group
	String name

	static hasMany = [documents:Document]

	static belongsTo = [bucket:Bucket]

	static constraints = {
		bucket nullable:true
		documents nullable:false
		group nullable:false
		name blank:false, nullable:false//, unique:'group'
	}

	static transients = ['asMap']

	def asMap() {
		[
			id:id,
			name:name,
			group:[
				id:group.id,
				name:group.name
			]
		]
	}
}
