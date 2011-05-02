<html>
	<head>
		<meta name="layout" content="main" />
		<jqui:resources theme="ui-lightness" />
		<link href="${resource(dir:'css', file:'tag.css')}" rel="stylesheet" media="screen, projection" />
		<g:javascript src="jquery.ui.touch-punch.min.js" />
		<g:javascript src="tagging.js" />
		<title> - Tag</title>
		<g:javascript>
			$(document).ready(Tagging.initDragAndDrop);
		</g:javascript>
	</head>
	<body>
		<g:render template="tagSearch" />
		<div id="untagged">
			<g:render template="untagged" />
		</div>
	</body>
</html>
