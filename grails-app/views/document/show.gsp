<!DOCTYPE html>
<html>
	<head>
		<title> - Show</title>
		<meta name="layout" content="main" />
		<link href="${resource(dir:'css', file:'document/show.css')}" rel="stylesheet" media="all" />
		<jqui:resources theme="ui-lightness" />
		<g:javascript src="jquery.ba-hashchange.js" />
		<g:javascript src="HtmlAlert.js" />
		<g:javascript src="document/document.js" />
		<g:javascript src="document/show.js" />
		<g:javascript>
			$(document).ready(function() {
				Show.init({
					'close': '${createLink(controller:"document", action:"index")}',
					'downloadImage': '${createLink(controller:"document", action:"downloadImage")}/{0}/{1}',
					'email': '${createLink(controller:"signatureCode", action:"send")}/{0}/{1}',
					'finish_redirect': '${createLink(controller:"document", action:"index")}',
					'image': '${createLink(controller:"document", action:"image")}/{0}/{1}',
					'print': '${createLink(controller:"printQueue", action:"push")}/{0}/{1}'
				});
			});
		</g:javascript>
	</head>
	<body>
		<input type="hidden" id="pageCount" value="${document?.previewImages?.size()}" />
		<input type="hidden" id="documentId" value="${document?.id}" />
		<button id="print" class="labeled-button" title="<g:message code="document-vault.label.print" />">
			<g:message code="document-vault.label.print" />
		</button>
		<a id="sign" class="labeled-button" href="${createLink(action:'sign', params:[documentId:document?.id])}" title="<g:message code="document-vault.label.sign" />">
			<g:message code="document-vault.label.sign" />
		</a>
		<button id="close" class="labeled-button" title="<g:message code="document-vault.label.close" />">
			<g:message code="document-vault.label.close" />
		</button>
		<h4 id="page-container"><g:message code="document-vault.label.page" />: <span id="page-number"></span></h4>
		<div id="main">
			<div id="left-arrow" class="arrow">
				<a href="#" title="<g:message code="document-vault.label.previouspage" />"><g:message code="document-vault.label.previouspage" /></a>
			</div>
			<div id="right-arrow" class="arrow">
				<a href="#" title="<g:message code="document-vault.label.nextpage" />"><g:message code="document-vault.label.nextpage" /></a>
			</div>
			<div id="canvas"></div>
		</div>
		<g:render template="printerDialog" />
		<%--<g:render template="emailDialog" />--%>
		<g:render template="/alert" />
	</body>
</html>
