<head>
	<meta name="layout" content="${grailsApplication.config.nimble.layout.administration}"/>
	<title><g:message code="nimble.view.user.list.title" /></title>
</head>
<body>
	<h2><g:message code="nimble.view.user.list.heading" /></h2>

	<g:remoteField value="${params.userFilter}"
		name="user-filter"
		id="userFilter"
		paramName="userFilter"
		url="[controller:'user', action:'list', params:[max:params.max, offset:params.offset, order:params.order, sort:params.sort]]"
		update="userlist-container" />

	<div id="userlist-container">
	<g:render template="list" model="${model}" />
	</div>
</body>
