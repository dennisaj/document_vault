<!DOCTYPE html>
<html>
	<head>
		<title> - Sign</title>
		<meta name="layout" content="mobile" />
		<link href="${resource(dir:'css', file:'document/sign.css')}" rel="stylesheet" media="all" />
		<jqui:resources theme="ui-lightness" />
		<g:javascript src="jquery.ba-hashchange.js" />
		<g:javascript src="HtmlAlert.js" />
		<g:javascript src="document/document.js" />
		<g:javascript src="document/sign.js" />
		<g:javascript>
			$(document).ready(function() {
				Sign.init({
					'close': '${createLink(controller:"document", action:"index")}',
					'downloadImage': '${createLink(controller:"document", action:"downloadImage")}/{0}/{1}',
					'email': '${createLink(controller:"signatureCode", action:"send")}/{0}/{1}',
					'finish_redirect': '${createLink(controller:"document", action:"index")}',
					'image': '${createLink(controller:"document", action:"image")}/{0}/{1}',
					'print': '${createLink(controller:"printQueue", action:"push")}/{0}/{1}',
					'sign': '${createLink(controller:"document", action:"submitSignatures")}/{0}'
				});
			});
		</g:javascript>
	</head>
	<body>
		<input type="hidden" id="pageCount" value="${document?.previewImages?.size()}" />
		<input type="hidden" id="documentId" value="${document?.id}" />
		<div id="buttonPanel">
			<hr />
			<pt:canSign document="${document}">
			<button id="save" class="labeled-button" title="<g:message code="document-vault.label.submitsignatures" />">
				<g:message code="document-vault.label.submitsignatures" />
			</button>
			<button id="pen" class="labeled-button mark" title="<g:message code="document-vault.label.pen" />">
				<g:message code="document-vault.label.pen" />
			</button>
			<button id="undo" class="labeled-button" title="<g:message code="document-vault.label.undo" />">
				<g:message code="document-vault.label.undo" />
			</button>
			<button id="clearcan" class="labeled-button" title="<g:message code="document-vault.label.clear" />">
				<g:message code="document-vault.label.clear" />
			</button>
			</pt:canSign>
			<pt:canPrint document="${document}">
			<button id="print" class="labeled-button" title="<g:message code="document-vault.label.print" />">
				<g:message code="document-vault.label.print" />
			</button>
			</pt:canPrint>
			<button id="zoomWidth" class="labeled-button" title="<g:message code="document-vault.label.zoomwidth" />">
				<g:message code="document-vault.label.zoomwidth" />
			</button>
			<button id="close" class="labeled-button" title="<g:message code="document-vault.label.close" />">
				<g:message code="document-vault.label.close" />
			</button>
			<h4 id="page-container"><g:message code="document-vault.label.page" />: <span id="page-number"></span></h4>
		</div>
		<div id="main">
			<div id="left-arrow" class="arrow">
				<a href="#" title="<g:message code="document-vault.label.previouspage" />"><g:message code="document-vault.label.previouspage" /></a>
			</div>
			<div id="right-arrow" class="arrow">
				<a href="#" title="<g:message code="document-vault.label.nextpage" />"><g:message code="document-vault.label.nextpage" /></a>
			</div>
			<canvas id="can" style="border: 1px solid #444;"></canvas>
		</div>
		<div id="dialog-message" title="<g:message code="document-vault.signature.wait.title" />">
			<p style="overflow: hidden;">
				<span class="ui-icon ui-icon-transferthick-e-w" style="float: left; margin: 0 7px 50px 0;"></span>
				<g:message code="document-vault.signature.wait.message" />
				<img src="${resource(dir:'images',file:'spinner.gif')}" alt="${message(code:'spinner.alt',default:'Loading...')}" />
			</p>
		</div>
		<g:render template="printerDialog" />
		<%--<g:render template="emailDialog" />--%>
		<g:render template="/alert" />
		<div id="confirm-submit" title="<g:message code="document-vault.signature.confirm.title" />">
			<p>
				<span class="ui-icon ui-icon-alert" style="float: left; margin: 0 7px 50px 0;"></span>
				<g:message code="document-vault.signature.confirm.message" />
			</p>
		</div>
		<div id="box" style="position: absolute;z-index:100"></div>
	</body>
</html>
