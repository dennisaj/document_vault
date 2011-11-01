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

grails.views.javascript.library='jquery'

nimble {
	fieldnames {
		uid = "uid"
		number = "number"
	}

	fields {
		admin {
			user = ['username', 'external']
		}
		enduser {
			user = ['username', 'pass', 'passConfirm']
			profile = ['fullName', 'email']
		}
	}

	passwords {
		mustcontain {
			lowercase = true
			uppercase = true
			numbers = true
			symbols = true
		}
		minlength = 8
	}

	organization {
		name = ""
		displayname = ""
		description = ""
		logo = ""
		logosmall = ""
		url = ""
	}

	layout {
		application = 'new'
		administration = 'admin'
		login = 'new'
	}

	resources {
		jslibrary = grails.views.javascript.library
		usejsdev = false
		usecssdev = false
	}

	localusers {
		authentication {
			enabled = true
		}
		registration {
			enabled = true
		}
		usernames {
			minlength = 4
			validregex = '[a-zA-Z0-9]*'
		}
		provision {
			active = false
		}
	}

	messaging {
		enabled = true

		registration {
			subject = "Your new account is ready!"
		}

		passwordreset {
			subject = "Your password has been reset"
		}

		changeemail {
			subject = "Your email address has been changed"
		}

		passwordreset {
			subject = "Your password has been reset"
			external.subject = "Your password reset request"
		}
	}
}

environments {
	development {
		nimble {
			resources {
				jslibrary = grails.views.javascript.library
				usejsdev = true
				usecssdev = true
			}
		}
	}
	production {
		nimble {
			resources {
				jslibrary = grails.views.javascript.library
				usejsdev = false
				usecssdev = false
			}
		}
	}
}
