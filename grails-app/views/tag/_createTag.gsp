<g:formRemote name="createForm" url="[action: 'create']" update="status" asynchronous="false">
	<div id="create">
		<label for="tagName"><g:message code="document-vault.label.newtag" /></label>
		<br />
		<input type="text" class="text" name="name" id="tagName" /><span id="status"></span>
	</div>
</g:formRemote>
