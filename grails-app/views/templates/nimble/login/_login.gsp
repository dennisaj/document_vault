<!DOCTYPE html>
<html>
<head>
	<meta name="layout" content="new"/>
	<title><g:message code="nimble.template.login.title" /></title>
	<r:require module="documentLogin" />
</head>
<body>

<div id="app-title" class="grid-9">
	<h1>Paperless Technologies Document Vault</h1>
	<h3>Electronic document storage and signature solutions.</h3>
</div>

<g:form action="signin" name="signin" class="grid-9">
	<n:flashembed/>

	<fieldset>
		<input type="hidden" name="targetUri" value="${targetUri}" />
		<input class="" id="username" type="text" name="username" placeholder="Username" autocapitalize="off" value="${params.username}" tabindex="1" />
		<input class="" id="password" type="password" name="password" placeholder="Password" tabindex="2" />
		<button class="right ui-button primary big" type="submit" tabindex="4"><g:message code="nimble.link.login.basic" /></button>
	</fieldset>

	<fieldset>
		<label for="rememberme" class="left check-label">
			<g:checkBox name="rememberme" value="${params.rememberme}" tabindex="3" />
			<g:message code="nimble.label.rememberme" />
		</label>

		<span class="right">
			<g:link controller="account" action="forgottenpassword" class="textlink icon icon_flag_purple"><g:message code="nimble.link.forgottenpassword" /></g:link>
			<g:if test="${registration}">
				&nbsp;|&nbsp;
				<g:link controller="account" action="createuser" class="textlink icon icon_user_go"><g:message code="nimble.link.newuser" /></g:link>
			</g:if>
		</span>
	</fieldset>
</g:form>

</body>
</html>
