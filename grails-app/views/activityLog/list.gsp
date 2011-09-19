<%@ page import="us.paperlesstech.ActivityLog" %>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="${grailsApplication.config.nimble.layout.administration}" />
		<g:set var="entityName" value="${g.message(code: 'activityLog.label', default: 'ActivityLog')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<h2><g:message code="default.list.label" args="[entityName]" /></h2>
		<table class="userlist">
			<thead>
				<tr>
					<th><g:message code="document-vault.view.activitylog.user" /></th>
					<g:sortableColumn params="${params}" property="dateCreated" title="${g.message(code: 'document-vault.view.activitylog.dateCreated')}" class="icon icon_arrow_refresh" />
					<g:sortableColumn params="${params}" property="action" title="${g.message(code: 'document-vault.view.activitylog.action')}" class="icon icon_arrow_refresh" />
					<g:sortableColumn params="${params}" property="document" title="${g.message(code: 'document-vault.view.activitylog.document')}" class="icon icon_arrow_refresh" />
					<g:sortableColumn params="${params}" property="pageNumber" title="${g.message(code: 'document-vault.view.activitylog.pageNumber')}" class="icon icon_arrow_refresh" />
					<td />
				</tr>
			</thead>
			<tbody>
			<g:each in="${activityLogInstanceList}" status="i" var="al">
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td><g:link action="show" controller="user" class="button icon icon_user_go" id="${al.user?.id}">${al.user?.username}</g:link></td>
					<td><g:formatDate date="${al.dateCreated}" /></td>
					<td>${al.action}</td>
					<td><g:link controller="activityLog" action="list" params="[documentId: al.document?.id]">${al.document}</g:link></td>
					<td>${al.pageNumber}</td>
					<td>
						<g:link action="show" class="button icon icon_table_go" id="${al.id}"><g:message code="nimble.link.view" /></g:link>
					</td>
				</tr>
			</g:each>
			</tbody>
		</table>
		<div class="paginateButtons">
			<g:paginate params="${params}" total="${activityLogInstanceTotal}" />
		</div>
	</body>
</html>
