package us.paperlesstech

import grails.plugin.multitenant.core.groovy.compiler.MultiTenant
import us.paperlesstech.nimble.Group

@MultiTenant
class Bucket {
	Date dateCreated
	Group group
	String name

	static hasMany = [folders:Folder]

	static constraints = {
		folders nullable:true
		group nullable:false
		name blank:false, nullable:false//, unique:'group'
	}

	static mapping = {
		folders cascade:'none'
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
