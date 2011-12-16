package us.paperlesstech

import grails.plugin.multitenant.core.CurrentTenant
import grails.plugin.multitenant.core.CurrentTenantAwareRepository
import grails.plugin.multitenant.core.Tenant
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Caches tenant resolution
 */
public class CachingTenantRepository implements CurrentTenantAwareRepository {
	private static final Logger log = LoggerFactory.getLogger(this)
	// will be injected
	CurrentTenant currentTenant

	@Override
	Tenant getCurrentTenant() {
		// TODO implement caching

		Tenant tenantInstance = null
		Integer currentTenantId = currentTenant.get()
		if (currentTenantId != null) {
			tenantInstance = findByTenantId(currentTenantId)
		}

		tenantInstance
	}

	@Override
	Tenant findByTenantId(Integer tenantId) {
		DomainTenantMap.findByMappedTenantId(tenantId)
	}
}