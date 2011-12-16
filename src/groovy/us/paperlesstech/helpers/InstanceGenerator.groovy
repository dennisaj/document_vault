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
package us.paperlesstech.helpers

import us.paperlesstech.nimble.Profile
import us.paperlesstech.nimble.User

/**
 * Determines correct version of class to load for classes commonly overloaded by host applications
 *
 * @author Bradley Beddoes
 */
class InstanceGenerator {
	// TODO: Inline this
	static user = {
		// As of Grails 2, this is not the same as
		// def user = new User(); user.profile = new Profile()
		// possibly a Grails bug? This works though
		def user = new User(profile: new Profile())
		user
	}
}
