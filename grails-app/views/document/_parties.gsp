<g:each in="${parties}" var="party">
	<g:render template="party" model="[document:document, party:party, signator:party?.signator, permissions:permissions, colors:colors]" />
</g:each>
