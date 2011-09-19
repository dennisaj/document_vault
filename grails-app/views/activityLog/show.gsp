<%@ page import="us.paperlesstech.ActivityLog" %>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="${grailsApplication.config.nimble.layout.administration}" />
		<g:set var="entityName" value="${g.message(code: 'activityLog.label', default: 'ActivityLog')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<h2><g:message code="default.show.label" args="[entityName]" /></h2>
		<div class="details" style="width:auto;">
			<h3><g:message code="document-vault.view.activitylog.show.details.heading" /></h3>
			<table class="datatable">
				<tbody>
					<tr class="prop">
						<td valign="top" class="name"><g:message code="document-vault.view.activitylog.id" /></td>
						<td valign="top" class="value">${activityLogInstance.id}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message code="document-vault.view.activitylog.user" /></td>
						<td valign="top" class="value"><g:link action="show" controller="user" class="button icon icon_user_go" id="${activityLogInstance.user?.id}">${activityLogInstance.user?.username}</g:link></td>
					</tr>

					<g:if test="${activityLogInstance.delegate}">
					<tr class="prop">
						<td valign="top" class="name"><g:message code="document-vault.view.activitylog.delegate" /></td>
						<td valign="top" class="value"><g:link action="show" controller="user" class="button icon icon_user_go" id="${activityLogInstance.delegate.id}">${activityLogInstance.delegate.username}</g:link></td>
					</tr>
					</g:if>

					<tr class="prop">
						<td valign="top" class="name"><g:message code="document-vault.view.activitylog.dateCreated" /></td>
						<td valign="top" class="value"><g:formatDate date="${activityLogInstance?.dateCreated}" /></td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message code="document-vault.view.activitylog.action" /></td>
						<td valign="top" class="value">${activityLogInstance.action}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message code="document-vault.view.activitylog.document" /></td>
						<td valign="top" class="value"><g:link controller="activityLog" action="list" params="[documentId: activityLogInstance.document?.id]">${activityLogInstance.document?.id}</g:link></td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message code="document-vault.view.activitylog.pageNumber" /></td>
						<td valign="top" class="value">${activityLogInstance.pageNumber}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message code="document-vault.view.activitylog.uri" /></td>
						<td valign="top" class="value">${activityLogInstance.uri}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message code="document-vault.view.activitylog.params" /></td>
						<td valign="top" class="value">${activityLogInstance.params}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message code="document-vault.view.activitylog.status" /></td>
						<td valign="top" class="value">${activityLogInstance.status}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message code="document-vault.view.activitylog.ip" /></td>
						<td valign="top" class="value">${activityLogInstance.ip}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message code="document-vault.view.activitylog.userAgent" /></td>
						<td valign="top" class="value">${activityLogInstance.userAgent}</td>
					</tr>
				</tbody>
			</table>
		</div>
	</body>
</html>
