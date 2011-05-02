<h3>Untagged documents:</h3>
<g:each var="document" in="${untagged}" status="index">
	<div class="draggable remove" documentid="${document.id}" id="drag-${document.id}"><img src="${resource(dir:'images', file:'document-blank.png')}" alt="" /><br />${document}</div>
</g:each>