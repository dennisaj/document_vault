<html>
	<head>
		<meta name="layout" content="main" />
		<jqui:resources theme="ui-lightness" />
		<link href="${resource(dir:'css', file:'jquery.fileupload-ui.css')}" rel="stylesheet" media="screen, projection" />
		<g:javascript src="jquery.fileupload.js" />
		<g:javascript src="jquery.fileupload-ui.js" />
		<g:javascript src="upload.js" />
		<title> - Upload</title>
	</head>
	<body>
		<g:if test="${tag}">
			<h3>Uploading to tag group <b>${tag}</b></h3>
		</g:if>
		<g:render template="/tag/recentTags" />
		<form id="file_upload" action="/document_vault/upload/save" method="POST" enctype="multipart/form-data">
			<input type="file" name="file" multiple>
			<button>Upload</button>
			<div class="file_upload_label">Upload files</div>
		</form>
		<table id="files"></table>
	</body>
</html>
