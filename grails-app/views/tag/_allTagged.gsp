<r:require module="dv-ui-tags" />
<h3><g:message code="document-vault.view.tag.documentstagged" args="[tag]" />:</h3>
<div class="droppable copy" tag="${tag}">
	<g:if test="${documents}">
		<g:each var="document" in="${documents}" status="index">
			<div class="draggable" documentid="${document.id}" id="drag-${document.id}">
				<img class="thumb" height="100" src="${createLink(controller:'document', action:'downloadImage', params:[documentId: document.id])}" /><br />
				${document} <a title="<g:message code="document-vault.view.tag.untag.title" />" href="javascript:Tagging.removeTag('${document.id}', '${tag}', function(){Tagging.showAllTagged('${tag}', '#allTagged');Tagging.showAllTagged('', '#untagged');})"><r:img uri="/images/tag-blue-delete.png" /></a>
			</div>
		</g:each>
	</g:if>
	<g:else>
		<span id="empty-message"><g:message code="document-vault.view.tag.nodocuments" /></span>
	</g:else>
</div>
<hr />
