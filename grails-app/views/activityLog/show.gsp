<%@ page import="us.paperlesstech.ActivityLog" %>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="${grailsApplication.config.nimble.layout.administration}" />
		<g:set var="entityName" value="${message(code: 'activityLog.label', default: 'ActivityLog')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<h2><g:message code="default.show.label" args="[entityName]" /></h2>
		<div class="details" style="width:auto;">
			<h3><g:message code="document-vault.view.activitylog.show.details.heading" /></h3>
			<table class="datatable">
				<tbody>
					<tr class="prop">
						<td valign="top" class="name"><g:message code="activityLog.id.label" default="Id" /></td>
						<td valign="top" class="value">${fieldValue(bean: activityLogInstance, field: "id")}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message code="activityLog.params.label" default="Params" /></td>
						<td valign="top" class="value">${fieldValue(bean: activityLogInstance, field: "params")}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message code="activityLog.user.label" default="User" /></td>
						<td valign="top" class="value"><g:link action="show" controller="user" class="button icon icon_user_go" id="${activityLogInstance.user?.id}">${activityLogInstance.user?.username?.encodeAsHTML()}</g:link></td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message code="activityLog.uri.label" default="Uri" /></td>
						<td valign="top" class="value">${fieldValue(bean: activityLogInstance, field: "uri")}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message code="activityLog.dateCreated.label" default="Date Created" /></td>
						<td valign="top" class="value"><g:formatDate date="${activityLogInstance?.dateCreated}" /></td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message code="activityLog.ip.label" default="Ip" /></td>
						<td valign="top" class="value">${fieldValue(bean: activityLogInstance, field: "ip")}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message code="activityLog.userAgent.label" default="User Agent" /></td>
						<td valign="top" class="value">${fieldValue(bean: activityLogInstance, field: "userAgent")}</td>
					</tr>
				</tbody>
			</table>
		</div>
	</body>
</html>
