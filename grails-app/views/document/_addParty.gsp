<div class="party ${hasErrors(bean: party, field: 'signator', 'ui-state-error')}" id="${code}">
	<input type="hidden" id="id-${code}" name="id-${code}" value="${party?.id}" />
	<g:radio id="radio-${code}" name="selectedParty" value="${code})" />
	<label for="fullName-${code}" class="${hasErrors(bean: party?.signator, field: 'profile.fullName', 'errors')}"><g:message code="document-vault.label.name" />:</label>
	<g:textField class="fullName" id="fullName-${code}" name="fullName-${code}" value="${party?.signator?.profile?.fullName}" />
	<label for="email-${code}" class="${hasErrors(bean: party?.signator, field: 'profile.email', 'errors')}"><g:message code="document-vault.label.email" />:</label>
	<g:textField class="email" id="email-${code}" name="email-${code}" value="${party?.signator?.profile?.email}" />
	<label for="expiration-${code}" class="${hasErrors(bean: party, field: 'expiration', 'errors')}"><g:message code="document-vault.label.expiration" />:</label>
	<g:textField class="expiration" id="expiration-${code}" name="expiration-${code}" value="${formatDate(date:party?.expiration, format:'MM/dd/yyyy')}" />
	<label for="color-${code}" class="${hasErrors(bean: party, field: 'color', 'errors')}"><g:message code="document-vault.label.color" />:</label>
	<g:select id="color-${code}" class="color" from="${colors}" optionKey="key" value="${party?.color?.name()}" />
	<label for="permission-${code}" class="${hasErrors(bean: party, field: 'documentPermission', 'errors')}"><g:message code="document-vault.label.permission" />:</label>
	<g:select name="permissionSelect" class="permission" id="permission-${code}" from="${permissions}" optionKey="key" value="${party?.documentPermission?.name()}" />
	<button class="remove" title="<g:message code="document-vault.label.remove" />">
		<g:message code="document-vault.label.remove" />
	</button>
	<br />
</div>
