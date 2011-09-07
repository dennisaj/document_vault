<!DOCTYPE html>
<html>
<head>
	<title> - <g:message code="document-vault.view.unauthorized.title" /></title>
	<meta name="layout" content="new" />
	<r:require module="documentSearch" />
</head>
<body>
<div class="subbar">
	<div class="ui-button-group">
		<a class="ui-button icon arrowleft" href="#" onclick="history.go(-1); return false;"><g:message code="document-vault.link.goback" /></a>
		<a class="ui-button icon reload" href=""><g:message code="document-vault.link.tryagain" /></a>
	</div>
</div>
<div class="message error"><g:message code="document-vault.view.unauthorized.message" /></div>
</body>
</html>
