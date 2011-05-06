beans = {
	xmlns aop: "http://www.springframework.org/schema/aop"

	businessLogicService(us.paperlesstech.handlers.business_logic.FermanBusinessLogicService) { bean ->
		bean.autowire = 'byName'
	}

	handlerAspect(us.paperlesstech.handlers.HandlerAspect) { bean ->
		bean.autowire = 'byName'
	}

	pngService(us.paperlesstech.handlers.PngHandlerService) { bean ->
		bean.autowire = 'byName'
	}

	pdfService(us.paperlesstech.handlers.PdfHandlerService) { bean ->
		bean.autowire = 'byName'
		nextService = pngService
	}

	handlerChain(us.paperlesstech.handlers.PclHandlerService) { bean ->
		bean.autowire = 'byName'
		nextService = pdfService
	}
}
