<!DOCTYPE html>
<html>
<head>
	<meta name="layout" content="new" />
	<title>- <g:message code="document-vault.view.tag.title" /></title>

	<r:require module="documentTagging" />
	<nav:resources override="true" />

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

			$('.thumb').live('click', function(event) {
				PreviewImage.show($(this).data('source-image'));
			});
			
			$('#tag-search-results ul').jcarousel({});
		});
	</r:script>
</head>
<body>

<g:render template="tagSearch" />

<div class="document-frame-wrapper">
	<div id="all-untagged" class="document-frames">
		<g:render template="untagged" />
	</div>
	
	<div id="all-tagged" class="document-frames">
		<h3>&nbsp;</h3>
	</div>
	
	<div class="droppable tag-remove"></div>
</div>

</body>
</html>
