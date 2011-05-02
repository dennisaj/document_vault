<!DOCTYPE html>
<html>
	<head>
		<title> - Sign</title>
		<meta name="layout" content="mobile" />
		<jqui:resources theme="ui-lightness" />
		<g:javascript src="jquery.ba-hashchange.js" />
		<g:javascript src="HtmlAlert.js" />
		<g:javascript src="document.js" />
		<g:javascript src="drawing.js" />
		<g:javascript>
			$(document).ready(function() {
				Drawing.init({
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
		<input type="hidden" id="pageCount" value="${document.previewImages.size()}" />
		<input type="hidden" id="documentId" value="${document.id}" />
		<div id="buttonPanel">
			<g:if test="${!document.signed()}">
				<input type="button" class="bigbutton" id="save" value="Submit Signatures" />
				<input type="button" class="bigbutton" id="pen" value="Pen" />
				<input type="button" class="bigbutton" id="undo" value="Undo" />
				<input type="button" class="bigbutton" id="clearcan" value="Clear" />
				<input type="button" class="bigbutton" id="email" value="Email" />
			</g:if>
			<g:else>
				<input type="button" class="bigbutton" id="print" value="Print" />
			</g:else>
			<input type="button" class="bigbutton" id="viewAll" value="View All" />
			<input type="button" class="bigbutton" id="close" value="Close" />
		</div>
		<div id="main">
			<div id="left-arrow" class="arrow">
				<a href="#">&lt;</a>
			</div>
			<div id="right-arrow" class="arrow">
				<a href="#">&gt;</a>
			</div>
			<canvas id="can" style="border: 1px solid #444;"></canvas>
			<canvas id="hidden-canvas" style="visibility: hidden; display: none"></canvas>
		</div>
		<div id="dialog-message" title="Saving Signatures">
			<p style="overflow: hidden;">
				<span class="ui-icon ui-icon-transferthick-e-w" style="float: left; margin: 0 7px 50px 0;"></span>
				Please wait while the captured signatures are uploaded...
				<div id="progressbar">
					<span id="pblabel" style="position: absolute; width: 90%; text-align: center;"> </span>
				</div>
			</p>
		</div>
		<g:render template="printerDialog" />
		<g:render template="emailDialog" />
		<g:render template="/alert" />
		<div id="confirm-submit" title="Confirm Submit">
			<p>
				<span class="ui-icon ui-icon-alert" style="float: left; margin: 0 7px 50px 0;"></span>
				Are you sure you want to want to submit the signature for this document?
			</p>
		</div>
	</body>
</html>

