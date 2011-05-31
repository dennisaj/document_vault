<%@ page import="us.paperlesstech.ActivityLog" %>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="${grailsApplication.config.nimble.layout.administration}" />
		<g:set var="entityName" value="${message(code: 'activityLog.label', default: 'ActivityLog')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<h2><g:message code="default.list.label" args="[entityName]" /></h2>
		<table class="userlist">
			<thead>
				<tr>
					<g:sortableColumn property="params" title="${message(code: 'activityLog.params.label', default: 'Params')}" class="icon icon_arrow_refresh" />
					<th><g:message code="activityLog.user.label" default="User" /></th>
					<g:sortableColumn property="uri" title="${message(code: 'activityLog.uri.label', default: 'Uri')}" class="icon icon_arrow_refresh" />
					<g:sortableColumn property="dateCreated" title="${message(code: 'activityLog.dateCreated.label', default: 'Date Created')}" class="icon icon_arrow_refresh" />
					<g:sortableColumn property="ip" title="${message(code: 'activityLog.ip.label', default: 'Ip')}" class="icon icon_arrow_refresh" />
					<td />
				</tr>
			</thead>
			<tbody>
			<g:each in="${activityLogInstanceList}" status="i" var="activityLogInstance">
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td>${fieldValue(bean: activityLogInstance, field: "params")}</td>
					<td><g:link action="show" controller="user" class="button icon icon_user_go" id="${activityLogInstance.user?.id}">${activityLogInstance.user?.username?.encodeAsHTML()}</g:link></td>
					<td>${fieldValue(bean: activityLogInstance, field: "uri")}</td>
					<td><g:formatDate date="${activityLogInstance.dateCreated}" /></td>
					<td>${fieldValue(bean: activityLogInstance, field: "ip")}</td>
					<td>
						<g:link action="show" class="button icon icon_table_go" id="${activityLogInstance.id}"><g:message code="nimble.link.view" /></g:link>
					</td>
				</tr>
			</g:each>
			</tbody>
		</table>
		<div class="paginateButtons">
			<g:paginate total="${activityLogInstanceTotal}" />
		</div>
	</body>
</html>
