def jqver = org.codehaus.groovy.grails.plugins.jquery.JQueryConfig.SHIPPED_VERSION

// Copied from the jq-ui resources configuration
// This is a bit ugly, we'll find a way to make this better in future
def appCtx = org.codehaus.groovy.grails.commons.ApplicationHolder.application.mainContext
def plugin = appCtx.pluginManager.getGrailsPlugin('jquery-ui')
def jquiver = plugin.instance.JQUERYUI_VERSION

// For explicitly disabling minification on a resource
//"myModule" {
//	resource url: [dir: "js", file: "myResourceThatShouldRemainUntouched.js"],  exclude:'minify'
//}

modules = {
	overrides {
		'jquery' {
			resource id: 'js', url:'https://ajax.googleapis.com/ajax/libs/jquery/'+jqver+'/jquery.min.js', disposition: 'head'
		}
		'jquery-ui' {
			dependsOn 'jquery'
			resource id: 'js', url: 'https://ajax.googleapis.com/ajax/libs/jqueryui/'+jquiver+'/jquery-ui.min.js', disposition: 'head'
		}
	}

	lessLibraries {
		resource url: '/less/bootstrap.less', attrs:[rel: 'stylesheet/less', type: 'css'], exclude: '*'
		resource url: '/less/base.less', attrs:[rel: 'stylesheet/less', type: 'css'], exclude: '*'
	}

	dvDefaults {
		resource url: '/css/lib/inuit.css'
		resource url: '/css/lib/grid.inuit.css'
		resource url: '/css/lib/buttons.css'
		resource url: '/css/lib/style.css'
	}

	dvTags {
		resource url: '/css/tagit-simple-blue.css', minify: true, nominify: false
		resource url: '/less/tag.less', attrs:[rel: 'stylesheet/less', type: 'css'], bundle:'dvTags'
		resource url: '/js/tagit.js'
		resource url: '/js/document/tagging.js'
		// TODO i18n text
		resource url: '/images/tag-blue-delete.png', attrs: [alt:'Delete Tag'], disposition: 'inline'
	}

	dvNotes {
		resource url: '/js/document/documentnote.js'
	}

	fileUpload {

		resource url: '/js/lib/jquery.tmpl.js'
		resource url: '/js/lib/jquery.iframe-transport.js'
		resource url: '/js/lib/jquery.fileupload.js'
		resource url: '/js/lib/jquery.fileupload-ui.js'
	}

	jqueryShowSign {
		dependsOn 'jquery-ui'

		resource url: '/js/jquery.ba-hashchange.js'
		resource url: '/js/global.js'
		resource url: '/js/document/document.js'
	}

	documentBase {
		dependsOn 'jquery, dvDefaults'

		resource url: '/js/new/base.js'
	}

	documentLogin {
		dependsOn 'jquery, dvDefaults'

		resource url: '/less/login.less', attrs:[rel: 'stylesheet/less', type: 'css'], bundle:'documentLogin'
		resource url: '/js/new/login.js'
	}

	documentSearch {
		dependsOn 'documentBase, jqueryShowSign'

		resource url: '/less/search.less', attrs:[rel: 'stylesheet/less', type: 'css'], bundle: 'documentSearch'
		resource url: '/js/new/search.js'
	}

	documentUpload {
		dependsOn 'documentBase, jquery-ui, fileUpload'

		resource url: '/less/upload.less', attrs:[rel: 'stylesheet/less', type: 'css'], bundle: 'documentUpload'
		resource url: '/js/new/upload.js'
	}

	documentShow {
		dependsOn 'documentBase, jqueryShowSign'

		resource url: '/less/show.less', attrs:[rel: 'stylesheet/less', type: 'css'], bundle: 'documentShow'
		resource url: '/js/new/show.js'
	}

	documentSign {
		dependsOn 'documentBase, jqueryShowSign, documentAlert'

		resource url: '/less/notes.less', attrs:[rel: 'stylesheet/less', type: 'css'], bundle: 'documentSign'
		resource url: '/less/sign.less', attrs:[rel: 'stylesheet/less', type: 'css'], bundle: 'documentSign'
		resource url: '/js/lib/jquery.textarea-expander.js'
		resource url: '/js/document/inputhandler.js'
		resource url: '/js/document/notes.js'
		resource url: '/js/document/draw.js'
		resource url: '/js/document/signbox.js'
		resource url: '/js/document/scratch.js'
		resource url: '/js/document/sign.js'
		resource url: '/js/document/party.js'
	}

	documentAlert {
		resource url: '/js/HtmlAlert.js'
	}

	documentTagging {
		dependsOn 'documentBase, jqueryShowSign, dvTags'

		resource url: '/js/lib/jquery.ui.touch-punch.min.js'
	}






	/*'dv-core' {
		defaultBundle 'core-ui'

		dependsOn 'jquery'

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

	'dv-ui-document' {
		dependsOn 'dv-ui-htmlalert', 'jquery-hashchange'

		resource url: '/js/document/document.js'
	}

	'dv-ui-htmlalert' {
		resource url: '/js/HtmlAlert.js'
	}

	'dv-ui-previewimage' {
		resource url: '/js/previewimage.js'
	}

	'dv-ui-show' {
		dependsOn 'dv-ui-document'

		resource url: '/css/document/show.css', minify: true, nominify: false
		resource url: '/js/document/show.js'
	}

	'dv-ui-overlay-notes' {
		resource url: '/css/document/notes.css', minify: true, nominify: false
		resource url: '/js/jquery.textarea-expander.js'
		resource url: '/js/document/notes.js'
	}

	'dv-ui-sign' {
		dependsOn 'dv-ui-document', 'dv-ui-overlay-notes'

		resource url: '/css/document/sign.css', minify: true, nominify: false
		resource url: '/js/document/inputhandler.js'
		resource url: '/js/document/draw.js'
		resource url: '/js/document/signbox.js'
		resource url: '/js/document/sign.js'
		resource url: '/js/document/party.js'
	}

	'dv-ui-tags' {
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
		dependsOn 'jquery-hashchange'

		resource url: '/js/document/documentsearch.js'
	}

	'dv-ui-upload' {
		dependsOn 'jquery-upload'

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
		dependsOn 'jquery-template'

		resource url: '/css/jquery.fileupload-ui.css', minify: true, nominify: false
		resource url: '/js/jquery.iframe-transport.js', minify: true, nominify: false
		resource url: '/js/jquery.fileupload.js', minify: true, nominify: false
		resource url: '/js/jquery.fileupload-ui.js', minify: true, nominify: false
	}*/
}
