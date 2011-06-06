<html>
	<head>
		<meta name="layout" content="main" />
		<jqui:resources theme="ui-lightness" />
		<link href="${resource(dir:'css', file:'tag.css')}" rel="stylesheet" media="screen, projection" />
		<g:javascript src="jquery.ui.touch-punch.min.js" />
		<g:javascript src="document/tagging.js" />
		<g:javascript src="previewimage.js" />
		<title> - Tag</title>
		<g:javascript>
			$(document).ready(function() {
				Tagging.init({
					'addTag': '${createLink(controller:"tag", action:"documentAdd")}',
					'allTagged': '${createLink(controller:"tag", action:"documents")}/{0}',
					'createTag': '${createLink(controller:"tag", action:"create")}/{0}',
					'documentList': '${createLink(controller:"tag", action:"documentList")}/{0}',
					'list': '${createLink(controller:"tag", action:"list")}',
					'removeTag': '${createLink(controller:"tag", action:"documentRemove")}'
				});
				Tagging.initDragAndDrop();

				$('.thumb').live('click', function(event) {
					PreviewImage.show(event.target.src);
				});
			});
		</g:javascript>
	</head>
	<body>
		<g:render template="tagSearch" />
		<div id="untagged">
			<g:render template="untagged" />
		</div>
	</body>
</html>
