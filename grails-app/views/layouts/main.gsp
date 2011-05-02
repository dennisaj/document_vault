<!DOCTYPE html>
<html>
	<head>
		<title>Paperless Technologies Document Vault<g:layoutTitle default="" /></title>
		<link href="${resource(dir:'images',file:'favicon.ico')}" rel="icon" />
		<link href="${resource(dir:'css',file:'main.css')}" rel="stylesheet" media="screen, projection" />
		<link href="${resource(dir:'css',file:'navigation.css')}" rel="stylesheet" media="screen, projection" />
		<blueprint:resources plugins="fancy-type" />
		<g:javascript library="jquery" plugin="jquery" />
		<g:javascript src="global.js" />
		<nav:resources override="true" />
		<g:layoutHead />
	</head>
	<body>
		<div id="spinner" class="spinner" style="display:none;">
			<img src="${resource(dir:'images',file:'spinner.gif')}" alt="${message(code:'spinner.alt',default:'Loading...')}" />
		</div>
		<div class="container">
			<div id="header" class="span-24 last">
				<h1>Paperless Technologies Document Vault</h1>
				<div class="span-16">
					<h3 class="alt">Electronic document storage and signature solutions.</h3>
				</div>
				<div class="span-8 last" style="text-align: right">
					<sec:ifLoggedIn>You are logged in as <sec:username />.</sec:ifLoggedIn>
				</div>
			</div>
			<hr />
			<div id="menu">
				<nav:render />
			</div>
			<g:render template="/layouts/messages" />
			
			<g:layoutBody />
		</div>
	</body>
</html>
