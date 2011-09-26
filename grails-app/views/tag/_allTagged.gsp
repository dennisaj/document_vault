<%@page import="grails.converters.JSON"%>
<h3><g:message code="document-vault.view.tag.documentstagged" args="[tag]" /></h3>

<div class="document-items droppable" data-tag="${tag}">
	<div class="active-drop-helper"></div>
	
	<g:if test="${documents}">
	
		<g:each var="document" in="${documents}" status="index">
		<div class="document">
			<p class="name">${document}</p>
			<div class="draggable thumb" data-source-image="${createLink(controller:'document', action:'downloadImage', params:[documentId:document.id, pageNumber:1])}" data-documentid="${document.id}" data-tag="${tag}" data-tags="${document.tags as JSON}">
				<img src="${createLink(controller:'document', action:'thumbnail', params:[documentId:document.id, pageNumber:1, documentDataId:document.previewImage(1).thumbnail.id])}" />
			</div>
		</div>
		</g:each>
		
	</g:if>
	<g:else>
		<span class="empty-message"><g:message code="document-vault.view.tag.nodocuments" /></span>
	</g:else>
	
</div>
