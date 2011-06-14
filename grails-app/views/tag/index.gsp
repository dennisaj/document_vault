<html>
	<head>
		<meta name="layout" content="main" />
		<r:require module="dv-ui-tags"/>
		<r:require module="jquery-touch-punch"/>
		<r:require module="dv-ui-previewimage"/>
		<r:script>
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
		</r:script>
		<title> - Tag</title>
	</head>
	<body>
		<g:render template="tagSearch" />
		<div id="untagged">
			<g:render template="untagged" />
		</div>
	</body>
</html>
