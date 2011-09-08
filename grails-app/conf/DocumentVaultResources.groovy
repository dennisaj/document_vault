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

	tagit {
		resource url: '/less/tag.less', attrs:[rel: 'stylesheet/less', type: 'css'], bundle:'tagit'
		resource url: '/css/tagit-simple-blue.css', minify: true, nominify: false
		resource url: '/js/lib/tagit.js'
	}

	dvTags {
		dependsOn 'tagit'

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

		resource url: '/js/lib/jquery.ba-hashchange.js'
		resource url: '/js/document/document.js'
	}

	documentPreview {
		dependsOn 'jquery-ui'

		resource url: '/js/previewimage.js'
	}

	documentBase {
		dependsOn 'jquery, dvDefaults'

		resource url: '/js/new/base.js'
		resource url: '/js/global.js'
	}

	documentLogin {
		dependsOn 'jquery, dvDefaults'

		resource url: '/less/login.less', attrs:[rel: 'stylesheet/less', type: 'css'], bundle:'documentLogin'
		resource url: '/js/new/login.js'
	}

	documentSearch {
		dependsOn 'documentBase, jqueryShowSign, documentPreview'

		resource url: '/less/search.less', attrs:[rel: 'stylesheet/less', type: 'css'], bundle: 'documentSearch'
		resource url: '/js/new/search.js'
	}

	documentUpload {
		dependsOn 'documentBase, jquery-ui, fileUpload, tagit'

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

		resource url: '/less/slider.less', attrs:[rel: 'stylesheet/less', type: 'css'], bundle: 'documentSign'
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
		dependsOn 'documentBase, jqueryShowSign, dvTags, documentPreview'

		resource url: '/js/lib/jquery.ui.touch-punch.min.js'
	}
}
