<g:each in="${document.notes.iterator().reverse()}" var="note">
	<g:if test="${note.note}">
		<g:message code="document-vault.view.note.noteText" args="[note.note, note.user.username, note.dateCreated]" encodeAs="HTML" /><br />
	</g:if>
</g:each>
