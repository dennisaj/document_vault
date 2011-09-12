<g:if test="${!params.pageNumber || params.int('pageNumber') < 1}">
	<g:set var="pageNumber" value="${1}" />
</g:if>
<g:elseif test="${params.int('pageNumber') > document?.previewImages?.size()}">
	<g:set var="pageNumber" value="${document?.previewImages?.size()}" />
</g:elseif>
<g:else>
	<g:set var="pageNumber" value="${params.int('pageNumber')}" />
</g:else>
<!DOCTYPE html>
<html>
<head>
	<meta name="layout" content="new" />
	<title> - <g:message code="document-vault.view.document.show.title" /></title>

	<r:require module="documentShow" />

	<r:script>
		$(document).ready(function() {
			Show.init({
				'close': '${createLink(controller:"document", action:"index")}',
				'downloadImage': '${createLink(controller:"document", action:"downloadImage")}/{0}/{1}',
				'finish_redirect': '${createLink(controller:"document", action:"index")}',
				'image': '${createLink(controller:"document", action:"image")}/{0}/{1}',
				'print': '${createLink(controller:"printQueue", action:"push")}/{0}/{1}',
				'printWindow': '${createLink(controller:"p", action:"window")}/{0}'
			});
		});
	</r:script>
</head>
<body>

<div id="show-actions">
	<input type="hidden" id="pageCount" value="${document?.previewImages?.size()}" />
	<input type="hidden" id="documentId" value="${document?.id}" />

	<div class="ui-button-group">
		<pt:canPrint document="${document}">
		<button id="print" class="ui-button icon lock" title="<g:message code="document-vault.label.print" />">
			<g:message code="document-vault.label.print" />
		</button>
		</pt:canPrint>

		<g:if test="${pt.canSign(document:document) || pt.canGetSigned(document:document)}">
		<a id="sign" class="ui-button icon edit" href="${createLink(action:'sign', params:[documentId:document?.id])}" title="<g:message code="document-vault.label.sign" />">
			<g:message code="document-vault.label.sign" />
		</a>
		</g:if>

		<button id="close" class="ui-button icon remove" title="<g:message code="document-vault.label.close" />">
			<g:message code="document-vault.label.close" />
		</button>
	</div>

	<span id="page-container"><g:message code="document-vault.label.page" /> <span id="page-number">${pageNumber}/${document?.previewImages?.size()}</span></span>
</div>

<div id="main">
	<div id="left-arrow" class="arrow left">
		<a class="ui-button" href="${createLink(action:'show', params:[documentId:document.id, pageNumber:(pageNumber - 1)])}" title="<g:message code="document-vault.label.previouspage" />"><r:img uri="/css/lib/images/document-page-previous-32x32.png" /></a>
	</div>

	<div id="right-arrow" class="arrow right">
		<a class="ui-button" href="${createLink(action:'show', params:[documentId:document.id, pageNumber:(pageNumber + 1)])}" title="<g:message code="document-vault.label.nextpage" />"><r:img uri="/css/lib/images/document-page-next-32x32.png" /></a>
	</div>

	<div id="canvas">
		<noscript>
			<img src="${createLink(controller:"document", action:"downloadImage", params:[documentId:document.id, pageNumber:pageNumber])}" />
		</noscript>
	</div>
</div>

<g:render template="/alert" />

</body>
</html>
