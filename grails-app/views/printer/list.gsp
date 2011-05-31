<%@ page import="us.paperlesstech.Printer" %>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="${grailsApplication.config.nimble.layout.administration}" />
		<g:set var="entityName" value="${message(code: 'printer.label', default: 'Printer')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<h2><g:message code="default.list.label" args="[entityName]" /></h2>
		<table class="userlist">
			<thead>
				<tr>
					<g:sortableColumn property="name" title="${message(code: 'printer.name.label', default: 'Name')}" class="icon icon_arrow_refresh" />
					<g:sortableColumn property="deviceType" title="${message(code: 'printer.deviceType.label', default: 'Device Type')}" class="icon icon_arrow_refresh" />
					<g:sortableColumn property="host" title="${message(code: 'printer.host.label', default: 'Host')}" class="icon icon_arrow_refresh" />
					<g:sortableColumn property="port" title="${message(code: 'printer.port.label', default: 'Port')}" class="icon icon_arrow_refresh" />
					<td />
				</tr>
			</thead>
			<tbody>
			<g:each in="${printerInstanceList}" status="i" var="printerInstance">
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td>${fieldValue(bean: printerInstance, field: "name")}</td>
					<td>${fieldValue(bean: printerInstance, field: "deviceType")}</td>
					<td>${fieldValue(bean: printerInstance, field: "host")}</td>
					<td>${printerInstance.port}</td>
					<td class="actionButtons">
						<span class="actionButton">
							<g:link action="show" id="${printerInstance.id}" class="button icon icon_printer"><g:message code="nimble.link.view" /></g:link>
						</span>
					</td>
				</tr>
			</g:each>
			</tbody>
		</table>
		<div class="paginateButtons">
			<g:paginate total="${printerInstanceTotal}" />
		</div>
	</body>
</html>
