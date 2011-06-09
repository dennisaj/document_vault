package us.paperlesstech.filters

import grails.plugin.multitenant.core.CurrentTenant
import grails.plugin.multitenant.core.TenantResolver

import java.io.IOException

import javax.servlet.FilterChain
import javax.servlet.ServletContext
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter
import org.springframework.context.ApplicationContext
import org.springframework.web.context.support.WebApplicationContextUtils

/**
 * This class was copied heavily from grails.plugin.multitenant.core.MultiTenantFilter
 * 
 */
class BasicAuthFilter extends BasicHttpAuthenticationFilter {

	private TenantResolver tenantResolver
	private CurrentTenant currentTenant

	private synchronized void checkInit(HttpServletRequest servletRequest) {
		if (tenantResolver == null || currentTenant == null) {
			ServletContext servletContext = servletRequest.getSession().getServletContext()
			ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);
			tenantResolver = ctx.tenantResolver
			currentTenant = ctx.currentTenant
		}
	}

	@Override
	public void doFilterInternal(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
		try {
			if (servletRequest instanceof HttpServletRequest) {
				HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest
				checkInit(httpServletRequest)
				Integer tenantId = tenantResolver?.getTenantFromRequest(httpServletRequest)
				currentTenant?.set tenantId
			}
			super.doFilterInternal(servletRequest, servletResponse, filterChain)
		} finally {
			currentTenant?.set 0
		}
	}
}
