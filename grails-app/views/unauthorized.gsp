<!DOCTYPE html>
<html>
	<head>
		<title> - <g:message code="document-vault.view.unauthorized.title" /></title>
		<meta name="layout" content="new" />
		<%--<r:require module="dv-core"/>--%>
	</head>
	<body>
		<br />
		<span class="error"><g:message code="document-vault.view.unauthorized.message" /></span><br /><br />
		<a href="#" onclick="history.go(-1); return false;"><g:message code="document-vault.link.goback" /></a> | 
		<a href=""><g:message code="document-vault.link.tryagain" /></a>
	</body>
</html>
