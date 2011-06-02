<html>
	<head>
		<meta name="layout" content="main" />
		<jqui:resources theme="ui-lightness" />
		<link href="${resource(dir:'css', file:'jquery.fileupload-ui.css')}" rel="stylesheet" media="screen, projection" />
		<g:javascript src="jquery.iframe-transport.js" />
		<g:javascript src="jquery.fileupload.js" />
		<g:javascript src="jquery.fileupload-ui.js" />
		<g:javascript src="jquery.iframe-transport.js" />
		<script src="http://ajax.aspnetcdn.com/ajax/jquery.templates/beta1/jquery.tmpl.js"></script>
		<g:javascript src="upload.js" />
		<g:javascript>
			$(document).ready(function() {
				Upload.init({
					'upload': '${createLink(controller:"upload", action:"ajaxSave")}'
				});
			});
		</g:javascript>
		<title> - <g:message code="document-vault.label.upload" /></title>
	</head>
	<body>
		<g:if test="${results}">
			<g:each var="result" in="${results}" status="index">
				<g:if test="${result.error}">
					<div class="error span-24 last">${result.name + ": " + result.error}</div>
				</g:if>
				<g:else>
					<div class="success span-24 last"><a href="${result.url}">${result.name}</a></div>
				</g:else>
			</g:each>
		</g:if>
		<g:if test="${groups}">
			<div id="fileupload">
				<form action="${createLink(action:"save")}" method="POST" enctype="multipart/form-data">
					<h4>
						<g:message code="document-vault.label.upload.group" />
						<g:select name="group" id="group" from="${groups}" optionKey="id" optionValue="name" value="${recentGroup?.id}" />
					</h4>
					<div class="fileupload-buttonbar">
						<label class="fileinput-button">
							<span><g:message code="document-vault.label.upload.addfiles" /></span>
							<input type="file" name="files" multiple>
						</label>
						<button type="submit" class="start"><g:message code="document-vault.label.upload.start" /></button>
						<button type="reset" class="cancel"><g:message code="document-vault.label.upload.cancel" /></button>
					</div>
				</form>
				<div class="fileupload-content">
					<table class="files"></table>
					<div class="fileupload-progressbar"></div>
				</div>
			</div>
			<g:set var="dollarSign" value="\$"></g:set>
			<script id="template-upload" type="text/x-jquery-tmpl">
				<tr class="template-upload{{if error}} ui-state-error{{/if}}">
					<td class="preview"></td>
					<td class="name">${dollarSign}{name}</td>
					<td class="size">${dollarSign}{sizef}</td>
					{{if error}}
						<td class="error" colspan="2">Error:
							{{if error === 'maxFileSize'}}<g:message code="document-vault.upload.error.maxsize" />
							{{else error === 'minFileSize'}}<g:message code="document-vault.upload.error.minsize" />
							{{else error === 'acceptFileTypes'}}<g:message code="document-vault.upload.error.filetype" />
							{{else error === 'maxNumberOfFiles'}}<g:message code="document-vault.upload.error.maxfiles" />
							{{else}}${dollarSign}{error}
							{{/if}}
						</td>
					{{else}}
						<td class="progress"><div></div></td>
						<td class="start"><button><g:message code="document-vault.label.start" /></button></td>
					{{/if}}
					<td class="cancel"><button><g:message code="document-vault.label.cancel" /></button></td>
				</tr>
			</script>
			<script id="template-download" type="text/x-jquery-tmpl">
				<tr class="template-download{{if error}} ui-state-error{{/if}}">
					{{if error}}
						<td></td>
						<td class="name">${dollarSign}{name}</td>
						<td class="size">${dollarSign}{sizef}</td>
						<td class="error" colspan="2"><g:message code="document-vault.label.error" />:
							{{if error === 1}}<g:message code="document-vault.upload.error.maxsize" />
							{{else error === 2}}<g:message code="document-vault.upload.error.maxsize" />
							{{else error === 3}}<g:message code="document-vault.upload.error.partialupload" />
							{{else error === 4}}<g:message code="document-vault.upload.error.noupload" />
							{{else error === 5}}<g:message code="document-vault.upload.error.tempfolder" />
							{{else error === 6}}<g:message code="document-vault.upload.error.writefail" />
							{{else error === 7}}<g:message code="document-vault.upload.error.uploadstopped" />
							{{else error === 'maxFileSize'}}<g:message code="document-vault.upload.error.maxsize" />
							{{else error === 'minFileSize'}}<g:message code="document-vault.upload.error.minsize" />
							{{else error === 'acceptFileTypes'}}<g:message code="document-vault.upload.error.filetype" />
							{{else error === 'maxNumberOfFiles'}}<g:message code="document-vault.upload.error.maxfiles" />
							{{else error === 'emptyResult'}}<g:message code="document-vault.upload.error.emptyresponse" />
							{{else}}${dollarSign}{error}
							{{/if}}
						</td>
					{{else}}
						<td class="preview">
							{{if thumbnail_url}}
								<a href="${dollarSign}{url}" target="_blank"><img src="${dollarSign}{thumbnail_url}"></a>
							{{/if}}
						</td>
						<td class="name">
							<a href="${dollarSign}{url}"{{if thumbnail_url}} target="_blank"{{/if}}>${dollarSign}{name}</a>
						</td>
						<td class="size">${dollarSign}{sizef}</td>
						<td colspan="2"></td>
					{{/if}}
					<td class="delete">
						<button data-type="${dollarSign}{delete_type}" data-url="${dollarSign}{delete_url}"><g:message code="document-vault.label.delete" /></button>
					</td>
				</tr>
			</script>
		</g:if>
		<g:else>
			<g:message code="document-vault.permission.error.upload" />
		</g:else>
	</body>
</html>
