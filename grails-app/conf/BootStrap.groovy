import grails.util.Environment

import us.paperlesstech.DomainTenantMap
import us.paperlesstech.Printer

class BootStrap {

	def tenantService

	def init = { servletContext ->
		assert new File("/usr/local/bin/pcl6")?.canExecute(), "Cannot execute /usr/local/bin/pcl6"
		assert new File("/usr/local/bin/gs")?.canExecute(), "Cannot execute /usr/local/bin/gs"

		// Disable JAI native acceleration layer
		System.setProperty('com.sun.media.jai.disableMediaLib', 'true')

		if (Environment.current == grails.util.Environment.DEVELOPMENT) {
			if (DomainTenantMap.count() == 0) {
				new DomainTenantMap(domainName:"localhost", mappedTenantId:1, name:"default").save()

				tenantService.initTenant(1) {
					if (Printer.count() == 0) {
						new Printer(name:"Recursive", host:"localhost", deviceType:"lj5gray", port:9100).save()
					}
				}
			}
		}
	}

	def destroy = {
	}
}
