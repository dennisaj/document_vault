<!DOCTYPE html>
<html>
	<head>
		<title> - <g:message code="document-vault.view.signature.title" /></title>
		<meta name="layout" content="mobile" />
		<r:require module="dv-ui-sign"/>
		<r:script>
			$(document).ready(function() {
				Sign.init({
					'close': '${createLink(controller:"document", action:"index")}',
					'downloadImage': '${createLink(controller:"document", action:"downloadImage")}/{0}/{1}',
					'finish_redirect': '${createLink(controller:"document", action:"index")}',
					'image': '${createLink(controller:"document", action:"image")}/{0}/{1}',
					'submitParties': '${createLink(controller:"document", action:"submitParties")}/{0}',
					'print': '${createLink(controller:"printQueue", action:"push")}/{0}/{1}',
					'removeParty': '${createLink(controller:"document", action:"removeParty")}/{0}/{1}',
					'resendCode': '${createLink(controller:"document", action:"resend")}/{0}/{1}',
					'sign': '${createLink(controller:"document", action:"submitSignatures")}/{0}'
				});
			});
		</r:script>
	</head>
	<body>
		<input type="hidden" id="pageCount" value="${document?.previewImages?.size()}" />
		<input type="hidden" id="documentId" value="${document?.id}" />
		<div id="buttonPanel">
			<hr />
			<pt:canSign document="${document}">
			<button id="save" class="labeled-button" title="<g:message code="document-vault.view.signature.submitsignatures" />">
				<g:message code="document-vault.view.signature.submitsignatures" />
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
			<g:if test="${pt.canGetSigned(document:document)}">
			<button id="get-signed" class="labeled-button" title="<g:message code="document-vault.view.signature.requestsignatures" />">
				<g:message code="document-vault.view.signature.requestsignatures" />
			</button>
			<button id="highlight" class="labeled-button mark" title="<g:message code="document-vault.label.highlight" />">
				<span id="sample"></span>
				<g:message code="document-vault.label.highlight" />
			</button>
			</g:if>
			<g:elseif test="${!parties.empty && pt.canSign(document:document)}">
			<button id="show-highlights" class="labeled-button" title="<g:message code="document-vault.view.signature.showhighlights" />">
				<g:message code="document-vault.view.signature.showhighlights" />
			</button>
			</g:elseif>
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
			<canvas id="can"></canvas>
		</div>
		<div id="signature-message" title="<g:message code="document-vault.view.party.wait.title" />">
			<p style="overflow: hidden;">
				<span class="ui-icon ui-icon-transferthick-e-w" style="float: left; margin: 0 7px 50px 0;"></span>
				<g:message code="document-vault.view.signature.wait.message" />
				<r:img uri='/images/spinner.gif' alt="${message(code:'spinner.alt',default:'Loading...')}" />
			</p>
		</div>

		<div id="party-message" title="<g:message code="document-vault.view.party.wait.title" />">
			<p style="overflow: hidden;">
				<span class="ui-icon ui-icon-transferthick-e-w" style="float: left; margin: 0 7px 50px 0;"></span>
				<g:message code="document-vault.view.party.wait.message" />
				<r:img uri='/images/spinner.gif' alt="${message(code:'spinner.alt',default:'Loading...')}" />
			</p>
		</div>
		<pt:canPrint document="${document}">
		<g:render template="printerDialog" />
		</pt:canPrint>
		<g:render template="requestSignature" />
		<g:render template="/alert" />
		<div id="confirm-submit" title="<g:message code="document-vault.view.signature.confirmsubmit.title" />">
			<p>
				<span class="ui-icon ui-icon-alert" style="float: left; margin: 0 7px 50px 0;"></span>
				<g:message code="document-vault.view.signature.confirmsubmit.message" />
			</p>
		</div>
		<pt:canGetSigned document="${document}">
		<div id="confirm-remove" title="<g:message code="document-vault.view.signature.confirmremove.title" />">
			<p>
				<span class="ui-icon ui-icon-alert" style="float: left; margin: 0 7px 50px 0;"></span>
				<g:message code="document-vault.view.signature.confirmremove.message" />
			</p>
		</div>
		</pt:canGetSigned>
		<div id="box" style="position: absolute;z-index:100"></div>
	</body>
</html>
