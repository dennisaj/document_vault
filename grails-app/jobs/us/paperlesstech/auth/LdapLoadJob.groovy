package us.paperlesstech.auth

import grails.plugin.multitenant.core.util.TenantUtils

class LdapLoadJob {
	static triggers = {
		simple name: 'loadLdapTrigger', startDelay: oneMinute, repeatInterval: 5 * oneMinute
	}
	private static final oneMinute = 1000 * 60
	def concurrent = false
	def grailsApplication

	def execute() {
		def ldapRealm = getBean()
		if (!ldapRealm.ldapEnabled) {
			log.info "Skipping ldap job because it is not enabled."
			return
		}

		// Currently LDAP only works when there is only one tenant
		def tenants = TenantUtils.allTenantData.values().collect { it.mappedTenantId } as Set
		assert tenants.size() == 1
		def tenantId = tenants.iterator().next()

		TenantUtils.doWithTenant(tenantId) {
			loadLdap()
		}
	}

	def loadLdap() {
		def ldapRealm = getBean()

		log.info "Running loadLdapGroups from ldap job"
		ldapRealm.loadLdapGroups()
		log.info "Completed loadLdapGroups from ldap job"

		log.info "Running loadLdapUsers from ldap job"
		ldapRealm.loadLdapUsers()
		log.info "Completed loadLdapUsers from ldap job"
	}

	def getBean() {
		// Because the LdapRealm is registered by the Shiro plugin after this job is injected,
		// we have to explicitly request it
		grailsApplication.mainContext.getBean("LdapRealmInstance")
	}
}
