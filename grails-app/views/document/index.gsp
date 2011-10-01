<!DOCTYPE html>
<html>
<head>
	<meta name="layout" content="new" />
	<title>- <g:message code="document-vault.view.document.search.title" /></title>

	<r:require module="documentSearch" />
	<r:require module="dvNotes" />

	<nav:resources override="true" />

	<r:script>
		$(document).ready(function() {
			Document.init({
				'close': '${createLink(controller:"document", action:"index")}',
				'downloadImage': '${createLink(controller:"document", action:"downloadImage")}/{0}/{1}',
				'finish_redirect': '${createLink(controller:"document", action:"index")}',
				'image': '${createLink(controller:"document", action:"image")}/{0}/{1}',
				'print': '${createLink(controller:"printQueue", action:"push")}/{0}/{1}',
				'sign': '${createLink(controller:"document", action:"sign")}/{0}',
				'printWindow': '${createLink(controller:"p", action:"window")}/{0}'
			});

			$('.thumb').live('click', function(event) {
				PreviewImage.show(event.currentTarget.href);
				return false;
			});

			DocumentSearch.init();
		});
	</r:script>
	
</head>
<body>
<div id="resultsHolder">
	<g:render template="searchResults" />
</div>

<g:render template="/alert" />

</body>
</html>
