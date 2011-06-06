<h3>Untagged documents:</h3>
<g:each var="document" in="${untagged}" status="index">
	<div class="draggable remove" documentid="${document.id}" id="drag-${document.id}"><img class="thumb" height="100" src="${createLink(controller:'document', action:'downloadImage', params:[documentId: document.id])}" /><br />${document}</div>
</g:each>
