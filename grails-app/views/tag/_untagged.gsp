<%@page import="grails.converters.JSON"%>
<h3><g:message code="document-vault.view.tag.documentsuntagged" /></h3>

<div class="document-items">

	<g:each var="document" in="${untagged}" status="index">
		<div class="document">
			<p class="name">${document}</p>
			<div class="draggable thumb remove" data-source-image="${createLink(controller:'document', action:'downloadImage', params:[documentId:document.id, pageNumber:1])}" data-documentid="${document.id}" data-tags="${document.tags as JSON}">
				<img src="${createLink(controller:'document', action:'thumbnail', params:[documentId:document.id, pageNumber:1, documentDataId:document.previewImage(1).thumbnail.id])}" />
			</div>
		</div>
	</g:each>

</div>