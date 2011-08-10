<div id="banner">
	<h1><g:message code="nimble.layout.admin.banner.heading" /></h1>

	<g:if test="${navigation}">
		<pt:isLoggedIn>
			<div id="userops">
				<g:message code="nimble.label.usergreeting" /> <pt:username /> | <g:link controller="auth" action="logout" class=""><g:message code="nimble.link.logout.basic" /></g:link>
			</div>
		</pt:isLoggedIn>
	</g:if>
</div>
