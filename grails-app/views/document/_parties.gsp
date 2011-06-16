<g:each in="${parties}" var="party">
	<g:render template="addParty" model="[party:party, code:party?.id?:party?.code, signator:party?.signator, permissions:permissions, colors:colors]" />
</g:each>
