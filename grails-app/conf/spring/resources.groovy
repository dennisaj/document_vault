beans = {
	authService(us.paperlesstech.AuthService) { bean ->
		bean.autowire = 'byName'
		bean.scope = 'request'
	}

	authServiceProxy(org.springframework.aop.scope.ScopedProxyFactoryBean){
		targetBeanName = 'authService'
		proxyTargetClass = true
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
