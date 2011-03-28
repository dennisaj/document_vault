<!DOCTYPE html>
<html>
	<head>
		<title>Paperless Technologies Document Vault<g:layoutTitle default="" /></title>
		<meta name="viewport" content="user-scalable=no, width=device-width, initial-scale=1.0, maximum-scale=1.0"/>
		<meta name="apple-mobile-web-app-capable" content="yes" />
		<meta name="apple-mobile-web-app-status-bar-style" content="black" />
		<link href="${resource(dir:'images',file:'favicon.ico')}" rel="icon" />
		<link href="${resource(dir:'css',file:'mobile.css')}" rel="stylesheet" media="screen, projection" />
		<link href="${resource(dir:'css', file:'iphone.css')}" rel="stylesheet" media="all" />
		<link href="${resource(dir:'css', file:'iphonep.css')}" rel="stylesheet" media="all and (orientation:landscape)" />
		<blueprint:resources plugins="fancy-type, link-icons" />
		<g:javascript library="jquery" plugin="jquery" />
		<g:javascript>
			function block(e) {
				e.preventDefault();
			} 
		</g:javascript>
		<g:layoutHead />
	</head>
	<body ontouchmove="block(event);">
		<div id="spinner" class="spinner" style="display:none;">
			<img src="${resource(dir:'images',file:'spinner.gif')}" alt="${message(code:'spinner.alt',default:'Loading...')}" />
		</div>
		<div class="container">
			<div id="header" class="span-24 last">
		  		<h1>Paperless Technologies Document Vault</h1>
		  		<div id="description" class="span-16">
					<h3 class="alt">Electronic document storage and signature solutions.</h3>
				</div>
				<div class="last" style="text-align: right">
					<sec:ifLoggedIn>You are logged in as <sec:username />.
					&nbsp;
					<a href="${createLink(controller: 'logout')}">Logout</a>
					</sec:ifLoggedIn>
				</div>
			</div>
		</div>
		<g:layoutBody />
	</body>
</html>
