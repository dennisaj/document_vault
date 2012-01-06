import com.mchange.v2.c3p0.ComboPooledDataSource

beans = {
	// Multi-Tenant beans
	tenantResolver(us.paperlesstech.DomainTenantResolver)
	tenantRepository(us.paperlesstech.CachingTenantRepository)

	dataSource(ComboPooledDataSource) { bean ->
		bean.destroyMethod = 'close'
		//use grails' datasource configuration for connection user, password, driver and JDBC url
		user = application.config.dataSource.username
		password = application.config.dataSource.password
		driverClass = application.config.dataSource.driverClassName
		jdbcUrl = application.config.dataSource.url
		//force connections to renew after 4 hours
		maxConnectionAge = 4 * 60 * 60
		//get rid too many of idle connections after 30 minutes
		maxIdleTimeExcessConnections = 30 * 60
		minPoolSize = 7
		maxPoolSize = 50
	}

	businessLogicService(us.paperlesstech.handlers.business_logic.FermanBusinessLogicService) { bean ->
		bean.autowire = 'byName'
	}

	tiffService(us.paperlesstech.handlers.TiffHandlerService) { bean ->
		bean.autowire = 'byName'
	}

	defaultImageService(us.paperlesstech.handlers.DefaultImageHandlerService) { bean ->
		bean.autowire = 'byName'
	}

	pdfService(us.paperlesstech.handlers.PdfHandlerService) { bean ->
		bean.autowire = 'byName'
	}

	pclService(us.paperlesstech.handlers.PclHandlerService) { bean ->
		bean.autowire = 'byName'
	}

	handlerChain(us.paperlesstech.handlers.HandlerChain) { bean->
		bean.autowire = 'byName'
		handlers = [pclService, pdfService, defaultImageService, tiffService]
	}
}

