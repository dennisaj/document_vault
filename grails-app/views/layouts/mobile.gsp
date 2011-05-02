<!DOCTYPE html>
<html>
	<head>
		<title>Paperless Technologies Document Vault<g:layoutTitle default="" /></title>
		<meta name="viewport" content="user-scalable=no, width=device-width, initial-scale=1.0, maximum-scale=1.0"/>
		<meta name="apple-mobile-web-app-capable" content="yes" />
		<meta name="apple-mobile-web-app-status-bar-style" content="black" />
		<link href="${resource(dir:'images',file:'favicon.ico')}" rel="icon" />
		<link href="${resource(dir:'css',file:'mobile.css')}" rel="stylesheet" media="screen, projection" />
		<blueprint:resources plugins="fancy-type, link-icons" />
		<g:javascript library="jquery" plugin="jquery" />
		<g:javascript src="global.js" />
		<g:javascript>
			function block(e) {
				e.preventDefault();
			} 
		</g:javascript>
		<g:layoutHead />
	</head>
	<body ontouchmove="block(event);" onload="setTimeout(function() { window.scrollTo(0, 1); }, 100);">
		<div id="spinner" class="spinner" style="display:none;">
			<img src="${resource(dir:'images',file:'spinner.gif')}" alt="${message(code:'spinner.alt',default:'Loading...')}" />
		</div>
		<div class="container">
			<div id="header" class="span-24 last">
		  		<h1>Paperless Technologies Document Vault</h1>
				<div class="last" style="text-align: right">
					<sec:ifLoggedIn>
						<a href="${createLink(controller: 'logout')}">Logout</a>
					</sec:ifLoggedIn>
				</div>
			</div>
		</div>
   		<g:render template="/layouts/messages" />
		<g:layoutBody />
	</body>
</html>
