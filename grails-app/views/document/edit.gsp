<!DOCTYPE html>
<html>
	<head>
		<title>Draw</title>
		<meta name="layout" content="mobile" />
		<jqui:resources theme="ui-lightness" />
		<g:javascript src="jquery.ba-hashchange.js" />
		<g:javascript src="iphone-live.js" />
	</head>
	<body>
		<input type="hidden" id="pageCount" value="${document.images.size()}" />
		<input type="hidden" id="documentId" value="${document.id}" />
		<div id="buttonPanel">
			<g:if test="${!document.signed}">
				<input type="button" class="bigbutton" id="save" value="Submit Signatures" />
				<input type="button" class="bigbutton" id="pen" value="Pen" />
				<input type="button" class="bigbutton" id="undo" value="Undo" />
				<input type="button" class="bigbutton" id="clearcan" value="Clear"/>
			</g:if>
			<input type="button" class="bigbutton" id="viewAll" value="View All" />
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
			<p>
				<span class="ui-icon ui-icon-transferthick-e-w" style="float: left; margin: 0 7px 50px 0;"></span>
				Please wait while the captured signatures are uploaded...
				<div id="progressbar">
					<span id="pblabel" style="position: absolute; width: 90%; text-align: center;"> </span>
				</div>
			</p>
		</div>
		<div id="confirm-submit" title="Confirm Submit">
			<p>
				<span class="ui-icon ui-icon-alert" style="float: left; margin: 0 7px 50px 0;"></span>
				Are you sure you want to want to submit the signature for this document?
			</p>
		</div>
		<div id="alert" title="Confirm Submit">
		</div>
	</body>
</html>

