<html>
	<head>
		<meta name="layout" content="main" />
		<jqui:resources theme="ui-lightness" />
		<title> - Search</title>
		<link href="${resource(dir:'css', file:'document-menu.css')}" rel="stylesheet" media="screen, projection" />
		<link href="${resource(dir:'css', file:'callout.css')}" rel="stylesheet" media="screen, projection" />
		<g:javascript src="jquery.jeditable.min.js" />
		<g:javascript src="documentnote.js" />
		<g:javascript src="HtmlAlert.js" />
		<g:javascript src="document.js" />
		<g:javascript>
			$(document).ready(function() {
				DocumentNote.init({ 'save': '/document_vault/document/saveNote' });
				Document.init({
						'email': '/document_vault/signatureCode/send/{0}/{1}',
						'finish': '/document_vault/document/finish/{0}',
						'image': '/document_vault/document/image/{0}/{1}',
						'print': '/document_vault/printQueue/push/{0}/{1}',
						'sign': '/document_vault/document/sign/{0}/{1}',
						'finish_redirect': '/document_vault/document/index',
						'close': '/document_vault/document/index'
				});
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
	<g:render template="emailDialog" />
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
				window.location.href = "/document_vault/document/index"
			});
			
			$("#advancedSearch").click(function() {
				$("#advancedPanel").toggle('fast');
			});
	</jq:jquery>
</body>
</html>
