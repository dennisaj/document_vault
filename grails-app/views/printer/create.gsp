<%@ page import="us.paperlesstech.Printer" %>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="${grailsApplication.config.nimble.layout.administration}" />
		<g:set var="entityName" value="${message(code: 'printer.label', default: 'Printer')}" />
		<title><g:message code="default.create.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="body">
			<h1><g:message code="default.create.label" args="[entityName]" /></h1>
			<g:hasErrors bean="${printerInstance}">
			<div class="error">
				<strong>Error</strong>
				<g:renderErrors bean="${printerInstance}" as="list" />
			</div>
			</g:hasErrors>
			<g:form action="save" >
				<div class="dialog">
					<table>
						<tbody>
							<tr class="prop">
								<td valign="top" class="name">
									<label for="name"><g:message code="printer.name.label" default="Name" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: printerInstance, field: 'name', 'errors')}">
									<g:textField name="name" value="${printerInstance?.name}" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="deviceType"><g:message code="printer.deviceType.label" default="Device Type" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: printerInstance, field: 'deviceType', 'errors')}">
									<g:textField name="deviceType" value="${printerInstance?.deviceType}" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="host"><g:message code="printer.host.label" default="Host" /></label> :
									<label for="port"><g:message code="printer.port.label" default="Port" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: printerInstance, field: 'host', 'errors')} ${hasErrors(bean: printerInstance, field: 'port', 'errors')}">
									<g:textField name="host" value="${printerInstance?.host}" /> : <g:textField name="port" value="${printerInstance?.port}" />
								</td>
							</tr>
						</tbody>
					</table>
				</div>
				<div class="buttons">
					<span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
				</div>
			</g:form>
		</div>
	</body>
</html>
