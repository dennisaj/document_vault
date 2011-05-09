beans = {
	xmlns aop: "http://www.springframework.org/schema/aop"

	businessLogicService(us.paperlesstech.handlers.business_logic.FermanBusinessLogicService)

	handlerAspect(us.paperlesstech.handlers.HandlerAspect) { bean ->
		bean.autowire = 'byName'
	}

	tiffService(us.paperlesstech.handlers.TiffHandlerService) { bean ->
		bean.autowire = 'byName'
	}

	defaultImageService(us.paperlesstech.handlers.DefaultImageHandlerService) { bean ->
		bean.autowire = 'byName'
		nextService = tiffService
	}

	pdfService(us.paperlesstech.handlers.PdfHandlerService) { bean ->
		bean.autowire = 'byName'
		nextService = defaultImageService
	}

	handlerChain(us.paperlesstech.handlers.PclHandlerService) { bean ->
		bean.autowire = 'byName'
		nextService = pdfService
	}
}
