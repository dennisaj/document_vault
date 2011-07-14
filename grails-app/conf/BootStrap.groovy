import grails.plugins.nimble.core.Group
import grails.plugins.nimble.core.Role

import us.paperlesstech.DomainTenantMap
import us.paperlesstech.Printer
import us.paperlesstech.User
import grails.util.Environment
import us.paperlesstech.flea.Flea

class BootStrap {
	def grailsApplication
	def groupService
	def tenantService

	def init = { servletContext ->
		assert new File("/usr/local/bin/pcl6")?.canExecute(), "Cannot execute /usr/local/bin/pcl6"
		assert new File("/usr/local/bin/gs")?.canExecute(), "Cannot execute /usr/local/bin/gs"
		assert new File("/usr/local/bin/convert")?.canExecute(), "Cannot execute /usr/local/bin/convert"
		assert new File(grailsApplication.config.document_vault.files.cache)?.isDirectory(), "Cache isn't a directory"
		assert new File(grailsApplication.config.document_vault.files.cache)?.canWrite(), "Can't write to cache"
		assert new File(grailsApplication.config.document_vault.files.cache)?.canRead(), "Can't read from cache"

		// Disable JAI native acceleration layer
		System.setProperty('com.sun.media.jai.disableMediaLib', 'true')

		// Perform any custom initialization
		def dvInit = grailsApplication.config.document_vault.init
		if (dvInit) {
			log.info("Performing custom bootstrapping")
			dvInit.delegate = delegate
			dvInit.call(servletContext)
		} else {
			log.info("no custom bootstrapping")
		}

		if (Environment.currentEnvironment == Environment.DEVELOPMENT) {
			if (DomainTenantMap.count() == 0) {
				new DomainTenantMap(domainName:"localhost", mappedTenantId:1, name:"default").save()

				tenantService.initTenant(1) {
					if (Printer.count() == 0) {
						new Printer(name:"LaserJet 5", host:"192.168.40.200", deviceType:"lj5gray", port:9100).save()
					}

					if (Group.count() == 0) {
						groupService.createGroup("test", "test", true)
					}
				}
			}
		}

		DomainTenantMap.list().each {
			tenantService.initTenant(it.mappedTenantId) {
				def signatorRole = Role.findByName(User.SIGNATOR_USER_ROLE)
				if (!signatorRole) {
					signatorRole = new Role()
					signatorRole.description = 'Issued to signator users'
					signatorRole.name = User.SIGNATOR_USER_ROLE
					signatorRole.protect = true
					signatorRole.save()

					if (signatorRole.hasErrors()) {
						signatorRole.errors.each {
							log.error it
						}
						throw new RuntimeException("Unable to create valid signator role for tenant $it.mappedTenantId")
					}
				}
			}
		}

		if (System.properties["flea.config.location"]) {
			// Make sure we can parse the Config outside of the thread so the bootstrap will fail on error
			def fleaConfig = new groovy.util.ConfigSlurper().parse(new File(System.properties["flea.config.location"])).toURL()
			def fleaThread = Thread.start {
				Flea.config = fleaConfig
				Flea.main()
			}
		}
	}

	def destroy = {
	}
}
