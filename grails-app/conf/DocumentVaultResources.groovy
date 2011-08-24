def jqver = org.codehaus.groovy.grails.plugins.jquery.JQueryConfig.SHIPPED_VERSION

// Copied from the jq-ui resources configuration
// This is a bit ugly, we'll find a way to make this better in future
def appCtx = org.codehaus.groovy.grails.commons.ApplicationHolder.application.mainContext
def plugin = appCtx.pluginManager.getGrailsPlugin('jquery-ui')
def jquiver = plugin.instance.JQUERYUI_VERSION

modules = {
	overrides {
		jquery {
			resource id: 'js', url:'https://ajax.googleapis.com/ajax/libs/jquery/'+jqver+'/jquery.min.js'
		}
		'jquery-ui' {
			resource id: 'js', url:'https://ajax.googleapis.com/ajax/libs/jqueryui/'+jquiver+'/jquery-ui.min.js'
		}
		'jquery-theme' {
			resource id: 'theme', url:'https://ajax.googleapis.com/ajax/libs/jqueryui/'+jquiver+'/themes/ui-lightness/jquery-ui.css'
		}
	}

	'dv-core' {
		defaultBundle 'core-ui'

		dependsOn 'jquery'
		dependsOn 'blueprint', 'blueprint-fancy-type'

		resource url: '/images/favicon.ico'
		resource url: '/js/global.js', minify: true
		resource url: '/images/spinner.gif', attrs: [:], disposition: 'inline'
	}

	'dv-desktop' {
		dependsOn 'dv-core'

		resource url: '/css/main.css', minify: true, nominify: false
		resource url: '/css/navigation.css', minify: true, nominify: false
	}

	'dv-login' {
		dependsOn 'dv-ui'

		resource url: '/css/nimble/login.css'
		resource url: '/js/nimble/jquery/nimbleui.js'
		resource url: '/js/nimble/jquery/jquery.url.js'
	}

	'dv-mobile' {
		dependsOn 'dv-core'

		resource url: '/css/mobile.css', minify: true
	}

	'dv-ui' {
		dependsOn 'jquery-ui', 'jquery-theme'
	}

	'dv-ui-document' {
		dependsOn 'dv-ui', 'dv-ui-htmlalert', 'jquery-hashchange'

		resource url: '/js/document/document.js'
	}

	'dv-ui-htmlalert' {
		dependsOn 'dv-ui'

		resource url: '/js/HtmlAlert.js'
	}

	'dv-ui-previewimage' {
		dependsOn 'dv-ui'

		resource url: '/js/previewimage.js'
	}

	'dv-ui-show' {
		dependsOn 'dv-ui-document'

		resource url: '/css/document/show.css', minify: true, nominify: false
		resource url: '/js/document/show.js'
	}

	'dv-ui-sign' {
		dependsOn 'dv-ui-document'

		resource url: '/css/document/sign.css', minify: true, nominify: false
		resource url: '/js/document/draw.js'
		resource url: '/js/document/signbox.js'
		resource url: '/js/document/sign.js'
		resource url: '/js/document/party.js'
	}

	'dv-ui-tags' {
		dependsOn 'dv-ui'

		resource url: '/css/tagit-simple-blue.css', minify: true, nominify: false
		resource url: '/css/tag.css', minify: true, nominify: false
		resource url: '/js/tagit.js'
		resource url: '/js/document/tagging.js'
		// TODO i18n text
		resource url: '/images/tag-blue-delete.png', attrs: [alt:'Delete Tag'], disposition: 'inline'
	}

	'dv-ui-notes' {
		resource url: '/js/document/documentnote.js'
	}

	'dv-ui-search' {
		dependsOn 'dv-ui', 'jquery-hashchange'

		resource url: '/js/document/documentsearch.js'
	}

	'dv-ui-upload' {
		dependsOn 'dv-ui', 'jquery-upload'

		resource url: '/js/upload.js'
	}

	'jquery-hashchange' {
		defaultBundle 'dv-jquery-plugins'

		resource url: '/js/jquery.ba-hashchange.js', minify: true, nominify: false
	}

	'jquery-template' {
		defaultBundle 'dv-jquery-plugins'

		resource url: '/js/jquery.tmpl.js'
	}

	'jquery-touch-punch' {
		defaultBundle 'dv-jquery-plugins'

		resource url: '/js/jquery.ui.touch-punch.min.js', minify: false, nominify: true
	}

	'jquery-upload' {
		defaultBundle 'dv-jquery-upload-plugins'
		dependsOn 'dv-ui', 'jquery-template'

		resource url: '/css/jquery.fileupload-ui.css', minify: true, nominify: false
		resource url: '/js/jquery.iframe-transport.js', minify: true, nominify: false
		resource url: '/js/jquery.fileupload.js', minify: true, nominify: false
		resource url: '/js/jquery.fileupload-ui.js', minify: true, nominify: false
	}
}
