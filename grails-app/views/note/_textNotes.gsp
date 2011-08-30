<g:each in="${document.notes.iterator().sort().reverse()}" var="note">
	<g:if test="${note.note}">
		<p><g:message code="document-vault.view.note.noteText" args="[note.note, note.user.username, note.dateCreated]" encodeAs="HTML" /></p>
	</g:if>
</g:each>
