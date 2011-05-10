beans = {
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
