package us.paperlesstech

import grails.plugin.multitenant.core.exception.TenantResolveException
import grails.plugin.multitenant.core.resolve.TenantResolver
import javax.servlet.http.HttpServletRequest

/**
 * Maps the host name to the tenant
 */
class DomainTenantResolver implements TenantResolver {
	Integer resolve(HttpServletRequest request) throws TenantResolveException {
		String host = request.getServerName()
		def tenant = DomainTenantMap.findByDomainName(host)

		assert tenant, "Unable to map host: '$host'"

		return tenant.tenantId()
	}
}