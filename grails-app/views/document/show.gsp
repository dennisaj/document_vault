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
		<hr />
		<button id="print" class="ui-button ui-widget ui-state-default ui-corner-all labeled-button" title="<g:message code="document-vault.label.print" />">
			<span class="ui-button-icon-primary ui-icon ui-icon-print"></span>
			<span class="ui-button-text"><g:message code="document-vault.label.print" /></span>
		</button>
		<a href="${createLink(action:'sign', params:[documentId:document?.id])}">
			<button id="sign" class="ui-button ui-widget ui-state-default ui-corner-all labeled-button" title="<g:message code="document-vault.label.sign" />">
				<span class="ui-button-icon-primary ui-icon ui-icon-pencil"></span>
				<span class="ui-button-text"><g:message code="document-vault.label.sign" /></span>
			</button></a>
		<button id="close" class="ui-button ui-widget ui-state-default ui-corner-all labeled-button" title="<g:message code="document-vault.label.close" />">
			<span class="ui-button-icon-primary ui-icon ui-icon-circle-close"></span>
			<span class="ui-button-text"><g:message code="document-vault.label.close" /></span>
		</button>
		<h4 id="page-container"><g:message code="document-vault.label.page" />: <span id="page-number"></span></h4>
		<div id="main">
			<div id="left-arrow" class="arrow">
				<a href="#">
					<button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only" title="<g:message code="document-vault.label.previouspage" />">
						<span class="ui-button-icon-primary ui-icon ui-icon-circle-arrow-w"></span>
						<span class="ui-button-text"><g:message code="document-vault.label.previouspage" /></span>
					</button></a>
			</div>
			<div id="right-arrow" class="arrow">
				<a href="#">
					<button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only" title="<g:message code="document-vault.label.nextpage" />">
						<span class="ui-button-icon-primary ui-icon ui-icon-circle-arrow-e"></span>
						<span class="ui-button-text"><g:message code="document-vault.label.nextpage" /></span>
					</button></a>
			</div>
			<div id="canvas"></div>
		</div>
		<g:render template="printerDialog" />
		<%--<g:render template="emailDialog" />--%>
		<g:render template="/alert" />
	</body>
</html>
