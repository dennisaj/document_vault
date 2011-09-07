<h3>Untagged documents:</h3>
<g:each var="document" in="${untagged}" status="index">
	<div class="draggable remove" data-documentid="${document.id}" id="drag-${document.id}">
		<img data-source-image="${createLink(controller:'document', action:'downloadImage', params:[documentId:document.id, pageNumber:1])}" class="thumb" height="100" src="${createLink(controller:'document', action:'thumbnail', params:[documentId:document.id, pageNumber:1, documentDataId:document.previewImage(1).thumbnail.id])}" />
		<br />${document}
	</div>
</g:each>
