<%@ page import="us.paperlesstech.Printer" %>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="${grailsApplication.config.nimble.layout.administration}" />
		<g:set var="entityName" value="${message(code: 'printer.label', default: 'Printer')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<h2><g:message code="default.show.label" args="[entityName]" /></h2>
		<div class="details">
			<h3><g:message code="document-vault.view.printer.show.details.heading" /></h3>
			<table class="datatable">
				<tbody>
					<tr class="prop">
						<td valign="top" class="name"><g:message code="printer.id.label" default="Id" /></td>
						<td valign="top" class="value">${fieldValue(bean: printerInstance, field: "id")}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message code="printer.name.label" default="Name" /></td>
						<td valign="top" class="value">${fieldValue(bean: printerInstance, field: "name")}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message code="printer.deviceType.label" default="Device Type" /></td>
						<td valign="top" class="value">${fieldValue(bean: printerInstance, field: "deviceType")}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message code="printer.host.label" default="Host" /></td>
						<td valign="top" class="value">${fieldValue(bean: printerInstance, field: "host")}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message code="printer.port.label" default="Port" /></td>
						<td valign="top" class="value">${printerInstance.port}</td>
					</tr>
				</tbody>
			</table>
		</div>
		<g:form name="deleteprinter" action="delete">
			<g:hiddenField name="id" value="${printerInstance?.id}" />
		</g:form>
	</body>
</html>
