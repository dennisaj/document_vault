<!DOCTYPE html>
<html>
	<head>
		<title>Paperless Technologies Document Vault<g:layoutTitle default="" /></title>
		<r:require module="dv-desktop" />
		<nav:resources override="true" />
		<g:layoutHead />
		<r:layoutResources/>
	</head>
	<body>
		<div id="spinner" class="spinner" style="display:none;">
			<r:img uri='/images/spinner.gif' alt="${message(code:'spinner.alt',default:'Loading...')}" />
		</div>
		<div class="container">
			<div id="header" class="span-24 last">
				<h1>Paperless Technologies Document Vault</h1>
				<div class="span-16">
					<h3 class="alt">Electronic document storage and signature solutions.</h3>
				</div>
				<div class="span-8 last" style="text-align: right">
					<n:isLoggedIn>You are logged in as <n:principal />.</n:isLoggedIn>
				</div>
			</div>
			<hr />
			<div id="menu">
				<nav:render />
			</div>
			<g:render template="/layouts/messages" />
			<g:layoutBody />
		</div>
		<r:layoutResources/>
	</body>
</html>
