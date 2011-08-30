<html>
	<head>
		<meta name="layout" content="new" />
		<r:require module="documentTagging"/>
		<r:script>
			$(document).ready(function() {
				Tagging.init({
					'addTag': '${createLink(controller:"tag", action:"documentAdd")}',
					'allTagged': '${createLink(controller:"tag", action:"documents")}',
					'createTag': '${createLink(controller:"tag", action:"create")}/{0}',
					'documentList': '${createLink(controller:"tag", action:"documentList")}/{0}',
					'list': '${createLink(controller:"tag", action:"list")}',
					'removeTag': '${createLink(controller:"tag", action:"documentRemove")}'
				});

				Tagging.initDragAndDrop();

				/*$('.thumb').live('click', function(event) {
					PreviewImage.show(event.target.src);
				});*/
			});
		</r:script>
		<title> - <g:message code="document-vault.view.tag.title" /></title>
	</head>
	<body>
		<g:render template="tagSearch" />
		<div id="untagged">
			<g:render template="untagged" />
		</div>
	</body>
</html>
