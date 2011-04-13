<html>
	<head>
		<meta name="layout" content="main" />
		<jqui:resources theme="ui-lightness" />
		<title> - Admin</title>
	</head>
	<body>
		<ul>
			<li>
				Printing
				<ul>
					<li><a href="${createLink(controller: 'printer', action: 'list')}">Printers</a></li>
					<li><a href="${createLink(controller: 'printQueue', action: 'list')}">Print Queue</a></li>
				</ul>
			</li>
			<li>
				Reports
				<ul>
					<li><a href="${createLink(controller: 'activityLog', action: 'list')}">Activity Log</a></li>
				</ul>
			</li>
		</ul>
	</body>
</html>
