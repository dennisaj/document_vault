class SslFilters {
	def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()
	def grailsApplication

	def filters = {
		all(controller: '*', action: '*') {
			before = {
				if (grailsApplication.config.document_vault.forceSSL) {
					if (request.getHeader('X-Forwarded-Proto') && request.getHeader('X-Forwarded-Proto') != 'https' && request.getHeader('User-Agent') != "ELB-HealthChecker") {
						def url = g.createLink(absolute: true, controller: controllerName, action: actionName, params: params)
						flash.each {key, value->
							flash[key] = value
						}
						redirect(url: url)
					}
				}
			}
		}
	}
}
