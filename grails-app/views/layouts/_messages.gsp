<g:if test="${flash.red}">
	<div class="message error">
			<strong><g:message code="document-vault.label.alert" />:</strong> ${flash.red}
	</div>
</g:if>

<g:if test="${flash.yellow}">
	<div class="message info">
		<strong><g:message code="document-vault.label.info" />:</strong> ${flash.yellow}
	</div>
</g:if>
<g:if test="${flash.green}">
	<div class="message success">
		${flash.green}
	</div>
</g:if>
