<!DOCTYPE html>
<html>
<head>
	<title> - <g:message code="document-vault.view.error.title" /></title>
	<meta name="layout" content="new" />
	<r:require module="documentSearch" />
</head>
<body>
<div class="subbar"><a class="ui-button icon arrowleft" href="#" onclick="history.go(-1); return false;"><g:message code="document-vault.link.goback" /></a></div>
<div class="message error"><g:message code="document-vault.view.error.message" /></div>

<g:if test="${grails.util.Environment.current == grails.util.Environment.DEVELOPMENT}">
	<h2>Error Details</h2>
	<strong>Error ${request.'javax.servlet.error.status_code'}:</strong> ${request.'javax.servlet.error.message'?.encodeAsHTML()}<br/>
	<div class="message">
		<strong>Servlet:</strong> ${request.'javax.servlet.error.servlet_name'}<br/>
		<strong>URI:</strong> ${request.'javax.servlet.error.request_uri'}<br/>
		<g:if test="${exception}">
			<strong>Exception Message:</strong> ${exception.message?.encodeAsHTML()} <br />
			<strong>Caused by:</strong> ${exception.cause?.message?.encodeAsHTML()} <br />
			<strong>Class:</strong> ${exception.className} <br />
			<strong>At Line:</strong> [${exception.lineNumber}] <br />
			<strong>Code Snippet:</strong><br />
			<div class="snippet">
				<g:each var="cs" in="${exception.codeSnippet}">
					${cs?.encodeAsHTML()}<br />
				</g:each>
			</div>
		</g:if>
	</div>
	<g:if test="${exception}">
		<h2>Stack Trace</h2>
		<div class="stack">
			<pre>${exception.stackTraceLines.join()}</pre>
		</div>
	</g:if>
</g:if>
</body>
</html>
