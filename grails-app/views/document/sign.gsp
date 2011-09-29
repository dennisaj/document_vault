<!DOCTYPE html>
<html>
<head>
	<title> - <g:message code="document-vault.view.signature.title" /></title>
	<meta name="layout" content="new" />
	<r:require module="documentSign" />
	<r:script>
		jQuery(function($) {
			// prevent text selection in IE9 or lower
			if ($.browser.msie && $.browser.version <= 9) {
				document.onselectstart = function() { return false; }
			}

			Sign.init({
				'close': '${createLink(controller:"document", action:"index")}',
				'downloadImage': '${createLink(controller:"document", action:"downloadImage")}/{0}/{1}',
				'finish_redirect': '${createLink(controller:"document", action:"index")}',
				'image': '${createLink(controller:"document", action:"image")}/{0}/{1}',
				'submitParties': '${createLink(controller:"party", action:"submitParties")}/{0}',
				'print': '${createLink(controller:"printQueue", action:"push")}/{0}/{1}',
				'removeParty': '${createLink(controller:"party", action:"removeParty")}/{0}/{1}',
				'resendCode': '${createLink(controller:"party", action:"resend")}/{0}/{1}',
				'sign': '${createLink(controller:"party", action:"submitSignatures")}/{0}',
				'listNotes': '${createLink(controller:"note", action:"list")}/{0}',
				'saveTextNote': '${createLink(controller:"note", action:"saveText")}/{0}',
				'printWindow': '${createLink(controller:"p", action:"window")}/{0}'
			});

		});
	</r:script>
	<pt:canNotes document="${document}">
		<r:script>
			jQuery(function($) {
				Notes.init();
			});
		</r:script>
	</pt:canNotes>
</head>
<body>
<input type="hidden" id="pageCount" value="${document?.previewImages?.size()}" />
<input type="hidden" id="documentId" value="${document?.id}" />

<div id="button-panel">
	<input type="hidden" id="pageCount" value="${document?.previewImages?.size()}" />
	<input type="hidden" id="documentId" value="${document?.id}" />

	<div class="ui-button-group">
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
		<g:if test="${pt.canGetSigned(document:document)}">
		<button id="get-signed" class="labeled-button" title="<g:message code="document-vault.view.signature.requestsignatures" />">
			<g:message code="document-vault.view.signature.requestsignatures" />
		</button>
		<button id="highlight" class="labeled-button mark" title="<g:message code="document-vault.label.highlight" />">
			<span id="sample"></span>
			<g:message code="document-vault.label.highlight" />
		</button>
		</g:if>
		<g:elseif test="${grailsApplication.config.document_vault.remoteSigning.enabled && !parties.empty && pt.canSign(document:document)}">
		<button id="show-highlights" class="labeled-button" title="<g:message code="document-vault.view.signature.showhighlights" />">
			<g:message code="document-vault.view.signature.showhighlights" />
		</button>
		</g:elseif>
		<pt:canNotes document="${document}">
		<button id="notes" class="mark labeled-button" title="<g:message code="document-vault.label.notes" />">
			<g:message code="document-vault.label.notes" />
		</button>
		</pt:canNotes>
		<button id="close" class="labeled-button" title="<g:message code="document-vault.label.close" />">
			<g:message code="document-vault.label.close" />
		</button>
	</div>
	<span id="page-container"><g:message code="document-vault.label.page" /> <span id="page-number">${pageNumber}/${document?.previewImages?.size()}</span></span>
</div>

<div id="main">
	<div id="slider-container">
		<button id="zoom-out" title="<g:message code="document-vault.label.zoomout" />">
			<g:message code="document-vault.label.zoomout" />
		</button>
		<div id="slider"></div>
		<button id="zoom-in" title="<g:message code="document-vault.label.zoomin" />">
			<g:message code="document-vault.label.zoomin" />
		</button>
	</div>
	<div id="left-arrow" class="arrow left">
		<a class="ui-button" href="#" title="<g:message code="document-vault.label.previouspage" />"><r:img uri="/css/lib/images/document-page-previous-32x32.png" /></a>
	</div>

	<div id="right-arrow" class="arrow right">
		<a class="ui-button" href="#" title="<g:message code="document-vault.label.nextpage" />"><r:img uri="/css/lib/images/document-page-next-32x32.png" /></a>
	</div>

	<canvas id="can" class="vml"></canvas>
</div>

<g:render template="/alert" />

<div id="confirm-submit" title="<g:message code="document-vault.view.signature.confirmsubmit.title" />">
	<p>
		<span class="ui-icon ui-icon-alert" style="float: left; margin: 0 7px 50px 0;"></span>
		<g:message code="document-vault.view.signature.confirmsubmit.message" />
	</p>
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

<g:render template="/party/requestSignature" />

<pt:canGetSigned document="${document}">
<div id="confirm-remove" title="<g:message code="document-vault.view.signature.confirmremove.title" />">
	<p>
		<span class="ui-icon ui-icon-alert" style="float: left; margin: 0 7px 50px 0;"></span>
		<g:message code="document-vault.view.signature.confirmremove.message" />
	</p>
</div>
</pt:canGetSigned>

<div id="box"></div>

</body>
</html>
