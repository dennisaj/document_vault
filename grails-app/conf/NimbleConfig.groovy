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

// change the JavaScript library that grails & nimble use by adjusting this value
// valid values: jquery, mootools prototype
grails.views.javascript.library='jquery'

nimble {
	organization {
		name = ""
		displayname = ""
		description = ""
		logo = ""
		logosmall = ""
		url = ""
	}

	layout {
		application = 'main'
		administration = 'admin'
		login = 'main'
	}

	resources {
		jslibrary = grails.views.javascript.library
	}

	localusers {
		authentication {
			enabled = true
		}
		registration {
			enabled = true
		}
	}

	facebook {
		federationprovider {
			enabled = false
			autoprovision = false
		}

		apikey = ""
		secretkey = ""
	}

	openid {
		federationprovider {
			enabled = false
			autoprovision = false
		}
	}

	messaging {
		enabled = false
		
		registration {
			subject = "Your new account is ready!"
		}
		passwordreset {
			subject = "Your password has been reset"
			external.subject = "Your password reset request"
		}

		mail {
			host = "smtp.gmail.com"
			port = 465
			username = "donotreply@paperlesstech.us"
			password = "ZgJ7Gy2W"
			props = ["mail.smtp.auth":"true",
					"mail.smtp.socketFactory.port":"465",
					"mail.smtp.socketFactory.class":"javax.net.ssl.SSLSocketFactory",
					"mail.smtp.socketFactory.fallback":"false"]
		}
	}

	implementation {
		user = "us.paperlesstech.User"
		profile = "us.paperlesstech.Profile"
	}

	tablenames {
		user = "users"
		role = "roles"
		group = "groups"
		federationprovider = "federation_provider"
		profilebase = "profile_base"
		loginrecord = "login_record"
		details = "details"
		permission = "permission"
		levelpermission = "level_permission"
		url = "url"
		socialmediaaccount = "social_media_account"
		socialmediaservice = "social_media_service"
	}
}

environments {
	development {
		nimble {
			recaptcha {
				enabled = false
				secureapi = false

				// These keys are generated by the ReCaptcha service
				publickey = ""
				privatekey = ""

				// Include the noscript tags in the generated captcha
				noscript = true
			}

			resources {
				jslibrary = grails.views.javascript.library
				usejsdev = true
				usecssdev = true
			}
		}
	}
	production {
		nimble {
			recaptcha {
				enabled = false
				secureapi = false

				// These keys are generated by the ReCaptcha service
				publickey = ""
				privatekey = ""

				// Include the noscript tags in the generated captcha
				noscript = true
			}

			resources {
				jslibrary = grails.views.javascript.library
				usejsdev = false
				usecssdev = false
			}
		}
	}
}
