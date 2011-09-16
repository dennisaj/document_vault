<g:if test="${delegators}">
	<g:form controller="runAs" action="runas" method="POST">
		<g:hiddenField name="targetUri" value="${request.forwardURI - request.contextPath}" />
		<g:select name="userId" from="${delegators}" optionValue="${{ it.profile?.fullName?:it.username}}" optionKey="id" />
		<g:submitButton class="ui-button" name="${g.message(code:'document-vault.label.switchuser')}" />
	</g:form>
</g:if>
