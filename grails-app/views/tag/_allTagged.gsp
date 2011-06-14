<r:require module="dv-ui-tags" />
<h3>Documents tagged <strong>${tag}</strong>:</h3>
<div class="droppable copy" tag="${tag}">
	<g:if test="${documents}">
		<g:each var="document" in="${documents}" status="index">
			<div class="draggable" documentid="${document.id}" id="drag-${document.id}">
				<img class="thumb" height="100" src="${createLink(controller:'document', action:'downloadImage', params:[documentId: document.id])}" /><br />
				${document} <a title="Click to untag this document" href="javascript:Tagging.removeTag('${document.id}', '${tag}', function(){Tagging.showAllTagged('${tag}', '#allTagged');Tagging.showAllTagged('', '#untagged');})"><r:img uri="/images/tag-blue-delete.png" /></a>
			</div>
		</g:each>
	</g:if>
	<g:else>
		<span id="empty-message">There are no documents with this tag.</span>
	</g:else>
</div>
<hr />
