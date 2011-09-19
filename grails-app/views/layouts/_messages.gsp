<div class="messages">
	<div class="message ondemand"></div>
	
	<g:if test="${flash.red}">
	<div class="message error">
			<b><g:message code="document-vault.label.alert" />:</b> ${flash.red}
	</div>
	</g:if>

	<g:if test="${flash.yellow}">
	<div class="message warning">
		<b><g:message code="document-vault.label.warning" />:</b> ${flash.yellow}
	</div>
	</g:if>

	<g:if test="${flash.blue}">
	<div class="message info">
		<b><g:message code="document-vault.label.info" />:</b> ${flash.blue}
	</div>
	</g:if>

	<g:if test="${flash.green}">
	<div class="message success">
		${flash.green}
	</div>
	</g:if>
</div>