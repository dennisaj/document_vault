<html>
	<head>
		<meta name="layout" content="main" />
		<jqui:resources theme="ui-lightness" />
		<title> - Search</title>
		<link href="${resource(dir:'css', file:'tagit-simple-blue.css')}" rel="stylesheet" media="screen, projection" />
		<link href="${resource(dir:'css', file:'tag.css')}" rel="stylesheet" media="screen, projection" />
		<g:javascript src="jquery.jeditable.min.js" />
		<g:javascript src="jquery.ba-hashchange.js" />
		<g:javascript src="tagit.js" />
		<g:javascript src="document/documentnote.js" />
		<g:javascript src="document/documentsearch.js" />
		<g:javascript src="HtmlAlert.js" />
		<g:javascript src="document/document.js" />
		<g:javascript src="document/tagging.js" />
		<g:javascript src="previewimage.js" />
		<g:javascript>
			$(document).ready(function() {
				DocumentNote.init({
					'save': '${createLink(controller:"document", action:"saveNote")}',
					'spinner': '${resource(dir:"images", file:"spinner.gif")}'
				});

				Document.init({
					'close': '${createLink(controller:"document", action:"index")}',
					'downloadImage': '${createLink(controller:"document", action:"downloadImage")}/{0}/{1}',
					'email': '${createLink(controller:"signatureCode", action:"send")}/{0}/{1}',
					'finish_redirect': '${createLink(controller:"document", action:"index")}',
					'image': '${createLink(controller:"document", action:"image")}/{0}/{1}',
					'print': '${createLink(controller:"printQueue", action:"push")}/{0}/{1}',
					'sign': '${createLink(controller:"document", action:"sign")}/{0}'
				});

				Tagging.init({
					'addTag': '${createLink(controller:"tag", action:"documentAdd")}',
					'allTagged': '${createLink(controller:"tag", action:"documents")}/{0}',
					'createTag': '${createLink(controller:"tag", action:"create")}/{0}',
					'documentList': '${createLink(controller:"tag", action:"documentList")}/{0}',
					'list': '${createLink(controller:"tag", action:"list")}',
					'removeTag': '${createLink(controller:"tag", action:"documentRemove")}'
				}, true);

				$('.thumb').live('click', function(event) {
					PreviewImage.show(event.target.src);
				});
				DocumentSearch.init();
			});
		</g:javascript>
	</head>
<body>
	<g:formRemote name="searchForm" url="[action: 'index']" update="resultsHolder" after="DocumentSearch.setHash(\$('#q').val())">
		<fieldset class="span-24 last">
			<legend>Search for a document</legend>
			<div id="search">
				<label for="q">Search</label><br />
				<g:textField name="q" value="${q}" class="text" />
				<button id="sub" type="submit" name="submit">
					<g:message code="document-vault.label.search" />
				</button>
				<button id="reset1" type="reset" name="reset">
					<g:message code="document-vault.label.clearform" />
				</button>
			</div>
		</fieldset>
	</g:formRemote>
	<div id="resultsHolder">
		<g:render template="searchResults" />
	</div>
	<g:render template="printerDialog" />
	<%--<g:render template="emailDialog" />--%>
	<g:render template="/alert" />
</body>
</html>
