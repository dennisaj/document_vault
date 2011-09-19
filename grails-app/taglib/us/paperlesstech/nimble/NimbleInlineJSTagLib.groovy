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
class NimbleInlineJSTagLib {
	static namespace = "njs"

	// Enables growl message popup when the Grails application stores a message in flash scope
	def flashgrowl = {attrs, body ->
		out << render(template: "/templates/inlinejs/jquery/flashgrowl", contextPath: pluginContextPath, model:[nimblePath:pluginContextPath])
	}

	// admin management
	def admin = {attrs ->
		out << render(template: "/templates/inlinejs/jquery/admin", contextPath: pluginContextPath, model:[nimblePath:pluginContextPath])
	}

	// User management
	def user = {attrs ->
		if (attrs.user == null) {
			throwTagError("User management tag requires user attribute [user]")
		}

		out << render(template: "/templates/inlinejs/jquery/user", contextPath: pluginContextPath, model:[nimblePath:pluginContextPath, user:attrs.user])
	}

	def permission = {attrs ->
		if (attrs.parent == null) {
			throwTagError("Permission management tag requires owner attribute [parent]")
		}

		out << render(template: "/templates/inlinejs/jquery/permission", contextPath: pluginContextPath, model:[nimblePath:pluginContextPath, parent:attrs.parent])
	}

	def role = {attrs ->
		if (attrs.parent == null) {
			throwTagError("Role management tag requires user attribute [parent]")
		}

		out << render(template: "/templates/inlinejs/jquery/role", contextPath: pluginContextPath, model:[nimblePath:pluginContextPath, parent:attrs.parent])
	}

	def group = {attrs ->
		if (attrs.parent == null) {
			throwTagError("Group management tag requires user attribute [parent]")
		}

		out << render(template: "/templates/inlinejs/jquery/group", contextPath: pluginContextPath, model:[nimblePath:pluginContextPath, parent:attrs.parent])
	}

	def member = {attrs ->
		if(attrs.parent == null) {
			throwTagError("Member management tag requires user attribute [parent]")
		}

		out << render(template: "/templates/inlinejs/jquery/member", contextPath: pluginContextPath, model:[nimblePath:pluginContextPath, parent:attrs.parent])
	}

	def delegator = {attrs ->
		if (attrs.parent == null) {
			throwTagError("Delegator management tag requires user attribute [parent]")
		}

		out << render(template: "/templates/inlinejs/jquery/delegator", contextPath: pluginContextPath, model:[nimblePath:pluginContextPath, parent:attrs.parent])
	}
}
