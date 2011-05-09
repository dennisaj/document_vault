package us.paperlesstech.handlers

import org.apache.commons.logging.LogFactory
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut

import us.paperlesstech.DocumentData
import us.paperlesstech.MimeType

@Aspect
class HandlerAspect {
	def businessLogicService
	def log = LogFactory.getLog(getClass())

	@Pointcut("""
			execution(* us.paperlesstech.handlers.*.*(..))
			&& @annotation(us.paperlesstech.handlers.InterceptHandler)
			""")
	def handlerMethods() {}

	@Around("handlerMethods()")
	def aroundHandler(ProceedingJoinPoint pjp) {
		def args = pjp.args
		def input = args[0]
		assert input instanceof Map, "@InterceptHandler methods must take a map as the first argument"

		def data = input.documentData
		assert data instanceof DocumentData, "@InterceptHandler methods must pass in documentData"

		def target = pjp.target
		def targetHandles = target.handlerFor

		assert targetHandles[0] instanceof MimeType, "Handler classes must declare static handlerFor = MimeType.something"

		def methodName = pjp.signature.name

		if (!targetHandles.contains(data.mimeType)) {
			target.nextService."$methodName"(*args)
			log.debug "Skipping execution of $pjp passing to the next handler passing to ${target.nextService}"
			return
		}
		log.debug "Executing $pjp"

		def businessMethod = "before${data.mimeType.toString()}${methodName.capitalize()}"
		if (businessLogicService?.metaClass?.respondsTo(businessLogicService, businessMethod)) {
			log.debug "calling $businessLogicService.$businessMethod"
			businessLogicService?."$businessMethod"(*args)
		} else {
			log.debug "$businessLogicService.$businessMethod does not exist"
		}

		pjp.proceed();

		businessMethod = "after${data.mimeType.toString()}$methodName"
		if (businessLogicService?.metaClass?.respondsTo(businessLogicService, businessMethod)) {
			log.debug "calling $businessLogicService.$businessMethod"
			businessLogicService?."$businessMethod"(*args)
		} else {
			log.debug "$businessLogicService.$businessMethod does not exist"
		}

		return
	}
}
