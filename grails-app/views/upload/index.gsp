<!DOCTYPE html>
<html>
<head>
	<meta name="layout" content="new"/>
	<title> - <g:message code="document-vault.label.upload" /></title>

	<r:require module="documentUpload" />

	<r:script>
		jQuery(function($) {
			Upload.init({
				'controller': '${createLink(controller:"upload", action:"ajaxSave")}',
				'createTag': '${createLink(controller:"tag", action:"create")}/{0}',
				'list': '${createLink(controller:"tag", action:"list")}'
			});
		});
	</r:script>
</head>
<body>

<g:if test="${results}">
	<g:each var="result" in="${results}" status="index">
		<g:if test="${result.error}">
			<div class="error">${result.name + ": " + result.error}</div>
		</g:if>
		<g:else>
			<div class="success"><a href="${result.url}">${result.name}</a></div>
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

			<pt:canTagAny>
			<div id="tag-container">
				<g:message code="document-vault.label.upload.tagging" />: <ul class="taggable" id="tagbox" data-name="tags"></ul>
			</div>
			</pt:canTagAny>

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
<li class="document template-upload{{if error}} error{{/if}}">
	<div class="thumb"><div class="real-progress"><div class="progress-ui"><div class="progress-ui-value"></div><span>&nbsp;</span></div></div></div>
	<p>
		<b>{{= name }}</b><br />
		{{if error}}
			<span>
			{{if error === 'maxFileSize'}}<g:message code="document-vault.upload.error.maxsize" />
			{{else error === 'minFileSize'}}<g:message code="document-vault.upload.error.minsize" />
			{{else error === 'acceptFileTypes'}}<g:message code="document-vault.upload.error.filetype" />
			{{else error === 'maxNumberOfFiles'}}<g:message code="document-vault.upload.error.maxfiles" />
			{{else}}{{= error}}
			</span>
		</p>
		{{/if}}
		{{else}}
			<span>{{= sizef }}</span>
		</p>
		<div class="ui-button-group document-actions">
			<button class="ui-button danger cancel"><g:message code="document-vault.label.cancel" /></button>
		</div>
		{{/if}}
		<div class="progress"><div></div></div>
</li>
</script>

<script id="template-download" type="text/html">
<li class="document template-download{{if error}} error{{/if}}">
	<div class="thumb">{{if thumbnail_url}}<a href="{{= url }}" target="_blank"><img src="{{= thumbnail_url }}"></a>{{/if}}</div>
	<p>
	{{if error}}
		<b>{{= name }}</b><br />
		<span>
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
		{{else}}{{= error}}</span>
		{{/if}}
	{{else}}
		<b><a href="{{= url }}"{{if thumbnail_url}} target="_blank"{{/if}}>{{= name }}</a></b><br />
		<span>{{= sizef }}</span>
	{{/if}}
	</p>
</li>
</script>

</g:if>

<g:else>
	<g:message code="document-vault.permission.error.upload" />
</g:else>

</body>
</html>
