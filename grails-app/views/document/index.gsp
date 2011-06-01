<html>
	<head>
		<meta name="layout" content="main" />
		<jqui:resources theme="ui-lightness" />
		<title> - Search</title>
		<link href="${resource(dir:'css', file:'document-menu.css')}" rel="stylesheet" media="screen, projection" />
		<link href="${resource(dir:'css', file:'callout.css')}" rel="stylesheet" media="screen, projection" />
		<link href="${resource(dir:'css', file:'tagit-simple-blue.css')}" rel="stylesheet" media="screen, projection" />
		<link href="${resource(dir:'css', file:'tag.css')}" rel="stylesheet" media="screen, projection" />
		<g:javascript src="jquery.jeditable.min.js" />
		<g:javascript src="tagit.js" />
		<g:javascript src="documentnote.js" />
		<g:javascript src="HtmlAlert.js" />
		<g:javascript src="document.js" />
		<g:javascript src="tagging.js" />
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
				});

				Tagging.initDragAndDrop();
			});
		</g:javascript>
	</head>
<body>
	<g:formRemote name="searchForm" url="[action: 'search']"
		update="searchResults">
		<fieldset class="span-24 last">
			<legend>Search for a document</legend>
			<g:hiddenField id="simpleSearch" name="simpleSearch" value="true" />
			<div id="search-tabs" class="ui-tabs">
				<ul>
					<li><a href="#search">Search</a>
					</li>
					<li><a href="#advancedSearch">Advanced Search</a>
					</li>
				</ul>
				<div id="search">
					<label for="q">Search</label><br /> <input type="text"
						class="text" name="q" id="q" /> <input type="submit"
						name="submit" value="Search" /> <input id='reset1' type="reset" name="reset"
						value="Clear Form" />
				</div>
				<div id="advancedSearch" class="ui-tabs-hide">
					<%--
					<g:select name="documentType" value="" id="documentType"
						from="${DocumentType.list()}" noSelection="['':'Document Type']"
						optionKey="id" optionValue="name"
						onchange="${remoteFunction(action:'documentTypeOptions', update:'documentTypeOptions', params:'\'documentTypeId=\'+$(\'#documentType\').val()',) }" />
					<div id="documentTypeOptions">&nbsp;</div>
					<input type="submit" name="submit" value="Search" /> <input
						id='reset2' type="reset" name="reset" value="Clear Form" />
						--%>
				</div>
			</div>
		</fieldset>
	</g:formRemote>
	<g:render template="searchResults" />
	<g:render template="printerDialog" />
	<%--<g:render template="emailDialog" />--%>
	<g:render template="/alert" />
	<jq:jquery>
		$("#q").focus();
		$("#search-tabs").tabs();
		$('#search-tabs').bind('tabsselect', function(event, ui) {
			if(ui.index == 0) {
				$("#simpleSearch").val("true")
			} else {
				$("#simpleSearch").val("false")
			}
		});
		
		$("#reset1").add("#reset2").click(function() {
			window.location.href = '${createLink(controller:"document", action:"index")}'
		});
		
		$("#advancedSearch").click(function() {
			$("#advancedPanel").toggle('fast');
		});
	</jq:jquery>
</body>
</html>
