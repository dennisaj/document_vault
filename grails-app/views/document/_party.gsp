<%-- If party.id is set, use that as the code. Otherwise use party.code --%>
<g:set var="code" value="${party?.id?:party?.code}" />
<%-- Disable the input fields if the party has been saved or if the user doesn't have the GetSigned permission --%>
<g:set var="disabled" value="${party?.id || !pt.canGetSigned(document:document)}" />
<div class="party ${hasErrors(bean: party, field: 'signator', 'ui-state-error')} ${hasErrors(bean: party, field: 'highlights', 'ui-state-error')}" id="${code}">
	<input type="hidden" id="id-${code}" name="id-${code}" value="${party?.id}" />
	<g:radio id="radio-${code}" name="selectedParty" value="${code})" />
	<g:select disabled="${disabled}" id="color-${code}" class="color" from="${colors}" optionKey="key" value="${party?.color?.name()}" />
	<label for="fullName-${code}" class="${hasErrors(bean: party?.signator, field: 'profile.fullName', 'errors')}"><g:message code="document-vault.label.name" />:</label>
	<pt:textField disabled="${disabled}" class="fullName" id="fullName-${code}" name="fullName-${code}" value="${party?.signator?.profile?.fullName}" />
	<g:if test="${pt.canGetSigned(document:document)}">
	<label for="email-${code}" class="${hasErrors(bean: party?.signator, field: 'profile.email', 'errors')}"><g:message code="document-vault.label.email" />:</label>
	<pt:textField autocapitalize="off" autocorrect="off" disabled="${disabled}" class="email" id="email-${code}" name="email-${code}" value="${party?.signator?.profile?.email}" />
	<label for="expiration-${code}" class="${hasErrors(bean: party, field: 'expiration', 'errors')}"><g:message code="document-vault.label.expiration" />:</label>
	<pt:textField disabled="${disabled}" class="expiration" id="expiration-${code}" name="expiration-${code}" value="${formatDate(date:party?.expiration, format:'MM/dd/yyyy')}" />
	</g:if>
	<label for="permission-${code}" class="${hasErrors(bean: party, field: 'documentPermission', 'errors')}"><g:message code="document-vault.label.permission" />:</label>
	<g:select disabled="${disabled}" name="permissionSelect" class="permission" id="permission-${code}" from="${permissions}" optionKey="key" value="${party?.documentPermission?.name()}" />

	<g:if test="${pt.canGetSigned(document:document)}">
		<g:if test="${party?.id && !party.completelySigned()}">
			<button class="resend" id="resend-${code}" title="<g:message code="document-vault.view.party.resend" />">
				<g:message code="document-vault.view.party.resend" />
			</button>
		</g:if>
		<g:message code="document-vault.label.status" />: <g:message code="${party.status()}" />
		<g:if test="${party?.removable()}">
		<button class="remove" title="<g:message code="document-vault.label.remove" />">
			<g:message code="document-vault.label.remove" />
		</button>
		</g:if>
	</g:if>
	<br />
</div>
