// Without this line LDAP started randomly failing for me
grails.naming.entries = null
// When true, saving a domain object throws an exception on save errors.
//grails.gorm.failOnError = false

// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts
grails.config.locations = [NimbleConfig]

if (System.properties["document_vault.config.location"]) {
	grails.config.locations << "file:" + System.properties["document_vault.config.location"]
}

if (System.properties["document_vault.init.location"]) {
	grails.config.locations << "file:" + System.properties["document_vault.init.location"]
}

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [html: ['text/html','application/xhtml+xml'],
					xml: ['text/xml', 'application/xml'],
					text: 'text/plain',
					js: 'text/javascript',
					rss: 'application/rss+xml',
					atom: 'application/atom+xml',
					css: 'text/css',
					csv: 'text/csv',
					all: '*/*',
					json: ['application/json','text/json'],
					form: 'application/x-www-form-urlencoded',
					multipartForm: 'multipart/form-data'
				]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

grails.app.context="/api"

grails.views.javascript.library="jquery"
// The default codec used to encode data with ${}
grails.views.default.codec = "html" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable for AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []

// Set resources plugin filter rules
grails.resources.cssrewriter.includes = ['**/*.css', '**/*.less']
grails.resources.csspreprocessor.includes = ['**/*.css', '**/*.less']

// set per-environment serverURL stem for creating absolute links
environments {
	production {
		grails.plugin.databasemigration.updateOnStart = true
		grails.plugin.databasemigration.updateOnStartFileNames = ['changelog.groovy']
		grails.serverURL = "http://www.changeme.com"
	}
	development {
		grails.plugin.databasemigration.updateOnStart = false
		grails.serverURL = "http://localhost:8080/api"
	}
	test {
		grails.plugin.databasemigration.updateOnStart = false
		grails.serverURL = "http://localhost:8080/api"
	}

}

// log4j configuration
log4j = {
	// Example of changing the log pattern for the default console
	// appender:
	//
	//appenders {
	//	 console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
	//}
	environments {
		production {
			appenders {
				rollingFile name: "dv_appender", file: "/var/log/jetty/document_vault.log"
			}

			root {
				info 'dv_appender'
			}
		}
	}

	info	'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
			'grails.app',
			'us.paperlesstech',
			'paperlesstech'

	error	'org.codehaus.groovy.grails.web.servlet',  //  controllers
			'org.codehaus.groovy.grails.web.pages', //  GSP
			'org.codehaus.groovy.grails.web.sitemesh', //  layouts
//			'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
			'org.codehaus.groovy.grails.web.mapping', // URL mapping
			'org.codehaus.groovy.grails.commons', // core / classloading
			'org.codehaus.groovy.grails.plugins', // plugins
			'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
			'org.springframework',
			'org.hibernate',
			'net.sf.ehcache.hibernate'

	warn	'org.mortbay.log'
	// Uncomment to log all SQL and parameters
	//debug  'org.hibernate.SQL'
	//debug 'org.grails.plugin.resource'
}

grails {
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

grails.mail.default.from = "Paperless Tech <donotreply@paperlesstech.us>"

tenant {
	domainTenantBeanName = "us.paperlesstech.DomainTenantMap"
	resolver.request.dns.type = "db"
}

security {
	shiro {
		authc.required = false
		filter.config = """\
[filters]
# HTTP Basic authentication
multitenant = grails.plugin.multitenant.core.MultiTenantFilter
authcBasic = org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter
authcBasic.applicationName = Document Vault API
[urls]
/api/** = multitenant, authcBasic
"""
	}
}

environments {
	production {
		document_vault.activity_log.enabled = true
		document_vault.remoteSigning.enabled = false
		document_vault.timing.enabled = true
		document_vault.files.cache = "/var/cache/document_vault/files"
		document_vault.forceSSL = true
	}
	development {
		document_vault.activity_log.enabled = false
		document_vault.remoteSigning.enabled = false
		document_vault.timing.enabled = true
		document_vault.files.cache = "/tmp"
		document_vault.forceSSL = false
		grails.converters.json.pretty.print = true
	}
	test {
		document_vault.activity_log.enabled = true
		document_vault.remoteSigning.enabled = false
		document_vault.timing.enabled = true
		document_vault.files.cache = "/tmp"
		document_vault.forceSSL = false
	}
}

document_vault.document.note.defaultHeight = 576
document_vault.document.note.defaultMimeType = us.paperlesstech.MimeType.PNG
document_vault.document.note.defaultWidth = 768

document_vault {
	aws {
		credentials {
			accessKey = "AKIAIXNHGGMEG62GBNZQ"
			secretKey = "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"
		}
		s3 {
			bucket = "pt_docvault_dev"
			cachePath = "/dv/cache"
		}
		sqs {
			logQueue = "https://queue.amazonaws.com/014589724006/dev-log-queue"
			// This must be 1-10
			logBatchSize = 10
		}
	}
}
