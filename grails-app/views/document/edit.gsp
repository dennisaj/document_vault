<!DOCTYPE html>
<html>
	<head>
		<title> - Sign</title>
		<meta name="layout" content="mobile" />
		<link href="${resource(dir:'css', file:'drawing.css')}" rel="stylesheet" media="all" />
		<jqui:resources theme="ui-lightness" />
		<g:javascript src="jquery.ba-hashchange.js" />
		<g:javascript src="HtmlAlert.js" />
		<g:javascript src="document.js" />
		<g:javascript src="drawing.js" />
		<g:javascript>
			$(document).ready(function() {
				Drawing.init({
						'close': '${createLink(controller:"document", action:"index")}',
						'downloadImage': '${createLink(controller:"document", action:"downloadImage")}/{0}/{1}',
						'email': '${createLink(controller:"signatureCode", action:"send")}/{0}/{1}',
						'finish_redirect': '${createLink(controller:"document", action:"index")}',
						'image': '${createLink(controller:"document", action:"image")}/{0}/{1}',
						'print': '${createLink(controller:"printQueue", action:"push")}/{0}/{1}',
						'sign': '${createLink(controller:"document", action:"sign")}/{0}'
				});
			});
		</g:javascript>
	</head>
	<body>
		<input type="hidden" id="pageCount" value="${document?.previewImages?.size()}" />
		<input type="hidden" id="documentId" value="${document.id}" />
		<div id="buttonPanel">
			<hr />
			<button class="bigbutton" id="save"><img src="${resource(dir:'images', file:'dialog-yes.png')}" alt="" /><br />Submit Signatures</button>
			<button class="bigbutton mark" id="pen"><img src="${resource(dir:'images', file:'favicon.ico')}" alt="" width="24" /><br />Pen</button>
			<%--<button class="bigbutton mark" id="highlight"><img src="${resource(dir:'images', file:'highlight.png')}" alt="" /><br />Highlight</button>--%>
			<button class="bigbutton" id="undo"><img src="${resource(dir:'images', file:'edit-undo.png')}" alt="" /><br />Undo</button>
			<button class="bigbutton" id="clearcan"><img src="${resource(dir:'images', file:'edit-clear.png')}" alt="" /><br />Clear</button>
			<%--<button class="bigbutton" id="email"><img src="${resource(dir:'images', file:'mail-send.png')}" alt="" /><br />Email</button>--%>
			<button class="bigbutton" id="print"><img src="${resource(dir:'images', file:'document-print.png')}" alt="" /><br />Print</button>
			<button class="bigbutton" id="zoomWidth"><img src="${resource(dir:'images', file:'zoom-fit-width.png')}" alt="" /><br />Zoom Width</button>
			<button class="bigbutton" id="close"><img src="${resource(dir:'images', file:'document-close.png')}" alt="" /><br />Close</button>
			<hr />
		</div>
		<div id="main">
			<div id="left-arrow" class="arrow">
				<a href="#">&lt;</a>
			</div>
			<div id="right-arrow" class="arrow">
				<a href="#">&gt;</a>
			</div>
			<canvas id="can" style="border: 1px solid #444;"></canvas>
		</div>
		<div id="dialog-message" title="Saving Signatures">
			<p style="overflow: hidden;">
				<span class="ui-icon ui-icon-transferthick-e-w" style="float: left; margin: 0 7px 50px 0;"></span>
				Please wait while the captured signatures are uploaded...
				<img src="${resource(dir:'images',file:'spinner.gif')}" alt="${message(code:'spinner.alt',default:'Loading...')}" />
			</p>
		</div>
		<g:render template="printerDialog" />
		<%--<g:render template="emailDialog" />--%>
		<g:render template="/alert" />
		<div id="confirm-submit" title="Confirm Submit">
			<p>
				<span class="ui-icon ui-icon-alert" style="float: left; margin: 0 7px 50px 0;"></span>
				Are you sure you want to want to submit the signature for this document?
			</p>
		</div>
		<div id="box" style="position: absolute;z-index:100"></div>
	</body>
</html>

