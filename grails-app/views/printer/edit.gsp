<%@ page import="us.paperlesstech.Printer" %>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="${grailsApplication.config.nimble.layout.administration}" />
		<g:set var="entityName" value="${message(code: 'printer.label', default: 'Printer')}" />
		<title><g:message code="default.edit.label" args="[entityName]" /></title>
	</head>
	<body>
		<h2><g:message code="default.edit.label" args="[entityName]" /></h2>
		<g:hasErrors bean="${printerInstance}">
		<div class="error">
			<strong>Error</strong>
			<g:renderErrors bean="${printerInstance}" as="list" />
		</div>
		</g:hasErrors>
		<g:form method="post" action="update">
			<g:hiddenField name="id" value="${printerInstance?.id}" />
			<g:hiddenField name="version" value="${printerInstance?.version}" />
			<div class="dialog">
				<table>
					<tbody>
						<tr class="prop">
							<td valign="top" class="name">
								<label for="name"><g:message code="printer.name.label" default="Name" /></label>
							</td>
							<td valign="top" class="value ${hasErrors(bean: printerInstance, field: 'name', 'errors')}">
								<g:textField name="name" value="${printerInstance?.name}" /><span class="icon icon_bullet_green">&nbsp;</span>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name">
								<label for="deviceType"><g:message code="printer.deviceType.label" default="Device Type" /></label>
							</td>
							<td valign="top" class="value ${hasErrors(bean: printerInstance, field: 'deviceType', 'errors')}">
								<g:textField name="deviceType" value="${printerInstance?.deviceType}" /><span class="icon icon_bullet_green">&nbsp;</span>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name">
								<label for="host"><g:message code="printer.host.label" default="Host" /></label> :
								<label for="port"><g:message code="printer.port.label" default="Port" /></label>
							</td>
							<td valign="top" class="value ${hasErrors(bean: printerInstance, field: 'host', 'errors')} ${hasErrors(bean: printerInstance, field: 'port', 'errors')}">
								<g:textField name="host" value="${printerInstance?.host}" /> :
								<g:textField name="port" value="${printerInstance?.port}" /><span class="icon icon_bullet_green">&nbsp;</span>
							</td>
						</tr>
						<tr>
							<td>
							</td>
							<td>
								<button class="button icon icon_printer" type="submit"><g:message code="document-vault.link.updateprinter" /></button>
								<g:link action="show" id="${printerInstance.id}" class="button icon icon_cross"><g:message code="nimble.link.cancel" /></g:link>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</g:form>
		<g:form name="deleteprinter" action="delete">
			<g:hiddenField name="id" value="${printerInstance?.id}" />
		</g:form>
	</body>
</html>
