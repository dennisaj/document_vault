/*
 *  Nimble, an extensive application base for Grails
 *  Copyright (C) 2010 Bradley Beddoes
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package us.paperlesstech.nimble

/**
 * Provides generic, mostly UI related tags to the Nimble application
 *
 * @author Bradley Beddoes
 */
class NimbleTagLib {
	static namespace = "n"

	/**
	 * Provides an inline output of the Grails application message in flash scope
	 */
	def flashembed = { attrs, body ->
		out << render(template: "/templates/flashembed", contextPath: pluginContextPath, model:[nimblePath:pluginContextPath])
	}

	/**
	 * Provides nimble session terminated message
	 */
	def sessionterminated = { attrs, body ->
		out << render(template: "/templates/sessionterminated", contextPath: pluginContextPath)
	}

	/**
	 * provides markup to render grails error messages for beans
	 */
	def errors = { attrs, body ->
		def bean = attrs['bean']
		if (bean)
		out << render(template: "/templates/errors", contextPath: pluginContextPath, model: [bean: bean])
		else
		out << render("Error: Details not supplied to generate error content")
	}

	/**
	 * Allows Nimble core and Host Apps alike to access images provided by Nimble
	 */
	def img = { attrs ->
		if (attrs.name == null || attrs.alt == null) {
			throwTagError("Image tag requires name and alt attributes")
		}

		def mkp = new groovy.xml.MarkupBuilder(out)
		mkp.img(src: resource(dir: pluginContextPath, file:"images/${attrs.name}"), alt: "$attrs.alt", width: attrs.size ?: '', height: attrs.size ?: '')
	}

	/**
	 * Allows Nimble core and Host Apps alike to access images provided for social sites
	 */
	def socialimg = { attrs ->
		if (attrs.alt == null || attrs.name == null || attrs.size == null) {
			throwTagError("Social image tag requires size, name and alt attributes")
		}

		def mkp = new groovy.xml.MarkupBuilder(out)
		mkp.img(src: resource(dir: pluginContextPath, file:"images/social/$attrs.size/${attrs.name}.png"), alt: "$attrs.alt")
	}

	/**
	* Allows UI developers to request confirmation from a user before performing some action
	*/
	def confirmaction = { attrs, body ->
		if (attrs.action == null || attrs.title == null || attrs.msg == null || attrs.accept == null || attrs.cancel == null) {
			throwTagError("Confirm action tag requires action, title, msg, accept and cancel attributes")
		}

		out << "<a href=\"#\" class=\"${attrs.class}\" onClick=\"confirmAction = function() { ${attrs.action} }; nimble.wasConfirmed('${attrs.title}', '${attrs.msg}', '${attrs.accept}', '${attrs.cancel}');\">${body()}</a>"
	}
	
	// Allows UI developers to request verification of contents of a field using onBlur
	def verifyfield = { attrs, body ->
		if (attrs.id == null || attrs.controller == null || attrs.action == null || attrs.name == null || attrs.validmsg == null || attrs.invalidmsg == null) {
			throwTagError("verifyfield tag requires id, controller, action, name, validmsg and invalidmsg attributes")
		}

		out << render(template: "/templates/tags/verifyfield", contextPath: pluginContextPath, model: [id:attrs.id, cssclass: attrs.class, required:attrs.required, controller:attrs.controller, action:attrs.action, name:attrs.name, value:attrs.value, validmsg:attrs.validmsg, invalidmsg:attrs.invalidmsg] )
	}

	def javascript = { attrs ->
		out << "<script type=\"text/javascript\" src=\"" + resource(dir: pluginContextPath, file: "/js/nimble/" + attrs.src) + "\"></script>"
	}

	def css = { attrs ->
		out << """<link rel="stylesheet" href=\"""" + resource(dir: pluginContextPath, file: "/css/nimble/" + attrs.src) + "\"/>"
	}
}
