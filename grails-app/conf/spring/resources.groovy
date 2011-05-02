// Place your Spring DSL code here
beans = {
	xmlns aop: "http://www.springframework.org/schema/aop"

	businessLogicService(us.paperlesstech.handlers.business_logic.FermanBusinessLogicService)

	handlerChain(us.paperlesstech.handlers.PclHandlerService) { bean ->
		bean.autowire = 'byName'
		nextService = pdfService(us.paperlesstech.handlers.PdfHandlerService) {
			nextService = pngService(us.paperlesstech.handlers.PngHandlerService)
		}
	}

	handlerAspect(us.paperlesstech.handlers.HandlerAspect) { bean ->
		bean.autowire = 'byName'
	}
//	xmlns aop: "http://www.springframework.org/schema/aop"
//
//	businessLogicService(us.paperlesstech.handlers.business_logic.FermanBusinessLogicService)
//
//	handlerChain(us.paperlesstech.handlers.PclHandlerService)
//
//	handlerAspect(us.paperlesstech.handlers.HandlerAspect) { bean ->
//		bean.autowire = 'byName'
//	}
//
//	us.paperlesstech.handlers.PclHandlerService {
//		nextHanlder = us.paperlesstech.handlers.PdfHandlerService
//	}
//
//	us.paperlesstech.handlers.PdfHandlerService {
//		nextHandler = us.paperlesstech.handlers.PngHandlerService
//	}
}
