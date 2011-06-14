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
		<title> - <g:message code="document-vault.view.document.show.title" /></title>
		<meta name="layout" content="main" />
		<r:require module="dv-ui-show"/>
		<r:script>
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
		</r:script>
	</head>
	<body>
		<input type="hidden" id="pageCount" value="${document?.previewImages?.size()}" />
		<input type="hidden" id="documentId" value="${document?.id}" />

		<pt:canPrint document="${document}">
		<button id="print" class="labeled-button" title="<g:message code="document-vault.label.print" />">
			<g:message code="document-vault.label.print" />
		</button>
		</pt:canPrint>
		<pt:canSign document="${document}">
		<a id="sign" class="labeled-button" href="${createLink(action:'sign', params:[documentId:document?.id])}" title="<g:message code="document-vault.label.sign" />">
			<g:message code="document-vault.label.sign" />
		</a>
		</pt:canSign>
		<button id="close" class="labeled-button" title="<g:message code="document-vault.label.close" />">
			<g:message code="document-vault.label.close" />
		</button>
		<h4 id="page-container"><g:message code="document-vault.label.page" />: <span id="page-number">${pageNumber}/${document?.previewImages?.size()}</span></h4>
		<div id="main">
			<div id="left-arrow" class="arrow">
				<a href="${createLink(action:'show', params:[documentId:document.id, pageNumber:(pageNumber - 1)])}" title="<g:message code="document-vault.label.previouspage" />">
				&lt; <g:message code="document-vault.label.previouspage" />
			</a>
			</div>
			<div id="right-arrow" class="arrow">
				<a href="${createLink(action:'show', params:[documentId:document.id, pageNumber:(pageNumber + 1)])}" title="<g:message code="document-vault.label.nextpage" />">
				<g:message code="document-vault.label.nextpage" /> &gt;
			</a>
			</div>
			<div id="canvas">
				<noscript>
				<img src="${createLink(controller:"document", action:"downloadImage", params:[documentId:document.id, pageNumber:pageNumber])}" />
				</noscript>
			</div>
		</div>
		<pt:canPrint document="${document}">
		<g:render template="printerDialog" />
		</pt:canPrint>
		<%--<g:render template="emailDialog" />--%>
		<g:render template="/alert" />
	</body>
</html>
