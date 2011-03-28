<!DOCTYPE html>
<html>
    <head>
        <title>Paperless Technologies Document Vault<g:layoutTitle default="" /></title>
		<link href="${resource(dir:'images',file:'favicon.ico')}" rel="icon" />
		<link href="${resource(dir:'css',file:'main.css')}" rel="stylesheet" media="screen, projection" />
		<blueprint:resources plugins="fancy-type, link-icons" />
        <g:layoutHead />
        <g:javascript library="jquery" plugin="jquery" />
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
                	<sec:ifLoggedIn>You are logged in as <sec:username />.
                	&nbsp;
                	<a href="${createLink(controller: 'logout')}">Logout</a>
                	</sec:ifLoggedIn>
                </div>
        	</div>
            <hr />
            
        	<g:layoutBody />
    	</div>
    </body>
</html>
