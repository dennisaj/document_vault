<!DOCTYPE html>
<html>
<head>
	<meta name="layout" content="new"/>
	<title> - <g:message code="document-vault.label.upload" /></title>
	
	<r:require module="documentUpload" />
	<%-- <r:require module="dv-ui-upload"/> --%>
	
	<r:script>
		var controller = "${createLink(controller:"upload", action:"ajaxSave")}";
	</r:script>
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
	<form action="${createLink(action:"save")}" method="post" enctype="multipart/form-data">
		
		<div class="fileupload-buttonbar ui-button-container">
			
			<g:if test="${groups.size() == 1}">
				<g:hiddenField name="group" value="${groups.first().id}"/>
			</g:if>
			<g:else>
				<g:message code="document-vault.label.upload.group" />
				<g:select name="group" id="group" from="${groups}" optionKey="id" optionValue="name" value="${recentGroup?.id}" />
			</g:else>
			

			<label class="fileinput-button primary icon add">
				<span><g:message code="document-vault.label.upload.addfiles" /></span>
				<input type="file" name="files" multiple>
			</label>
		</div>
		
	</form>
	
	<div class="fileupload-content">
		<ul class="files"></ul>
		<div class="fileupload-progressbar"></div>
	</div>
	
</div>

<script id="template-upload" type="text/html">
<li class="template-upload{{if error}} error{{/if}}">
	{{if error}}
		<p>{{= name }}&nbsp;&rarr;&nbsp;
		{{if error === 'maxFileSize'}}<g:message code="document-vault.upload.error.maxsize" />
		{{else error === 'minFileSize'}}<g:message code="document-vault.upload.error.minsize" />
		{{else error === 'acceptFileTypes'}}<g:message code="document-vault.upload.error.filetype" />
		{{else error === 'maxNumberOfFiles'}}<g:message code="document-vault.upload.error.maxfiles" />
		{{else}}{{= error}}</p>
	{{/if}}
	{{else}}
		<div class="progress"><div></div></div>		
		<p class="name">{{= name }} ({{= sizef }})</p>
	{{/if}}
	<div class="preview"></div>
	<span class="cancel"><button class="ui-button big danger icon trash"><g:message code="document-vault.label.cancel" /></button></span>
</li>
</script>

<script id="template-download" type="text/html">
<li class="template-download{{if error}} error{{/if}}">
	{{if error}}
		<p>{{= name }}&nbsp;&rarr;&nbsp;
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
		{{else}}{{= error}}</p>
	{{/if}}
	{{else}}
		<p class="name"><a href="{{= url }}"{{if thumbnail_url}} target="_blank"{{/if}}>{{= name }}</a> ({{= sizef }})</p>
	{{/if}}
	<div class="preview">
		{{if thumbnail_url}}
		<a href="{{= url }}" target="_blank"><img src="{{= thumbnail_url }}"></a>
		{{/if}}
	</div>
</li>
</script>

<script id="template-download-original" type="text/x-jquery-tmpl">
<tr class="template-download{{if error}} ui-state-error{{/if}}">
	{{if error}}
	<td></td>
	<td class="name">{{= name }}</td>
	<td class="size">{{= sizef }}</td>
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
	{{else}}{{= error}
	{{/if}}
	</td>
	{{else}}
	<td class="preview">
		{{if thumbnail_url}}
		<a href="{{= url }}" target="_blank"><img src="{{= thumbnail_url }}"></a>
		{{/if}}
	</td>
	<td class="name">
		<a href="{{= url }}"{{if thumbnail_url}} target="_blank"{{/if}}>{{= name }}</a>
	</td>
	<td class="size">{{= sizef }}</td>
	<td colspan="2"></td>
	{{/if}}
	<td class="delete">
		<button data-type="{{= delete_type }}" data-url="{{= delete_url }}"><g:message code="document-vault.label.delete" /></button>
	</td>
</tr>
</script>

</g:if>

<g:else>
	<g:message code="document-vault.permission.error.upload" />
</g:else>
		
</body>
</html>
