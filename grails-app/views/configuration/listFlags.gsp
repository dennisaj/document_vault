<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="${grailsApplication.config.nimble.layout.administration}"/>
	<title><g:message code="document-vault.link.flags"/></title>
</head>

<body>
<h2><g:message code="document-vault.link.flags"/></h2>

<div id="flagList" class="section">
	<g:render template="flags"/>
</div>

<g:formRemote name="addFlag" url="[action: 'addFlag']" update="flagList" onComplete="jQuery('#flag').val('')">
	Flag to add: <g:textField id="flag" name="flag"/>
</g:formRemote>
</body>
</html>

