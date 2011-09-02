<!DOCTYPE html>
<html lang="en" class="">
<head>
	<meta charset="UTF-8" />
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" /> 
	<meta name="viewport" content="user-scalable=no, width=device-width, minimum-scale=1.0, maximum-scale=1.0" />
	<meta name="format-detection" content="telephone=no">
	
	<title>Paperless Technologies Document Vault<g:layoutTitle default="" /></title>
	
	<link rel="shortcut icon" href="icon.png" />
	<link rel="apple-touch-icon-precomposed" href="icon.png" />
	
	<g:layoutHead />
	<r:layoutResources/>
	<r:script>
		jQuery(function($) {
			$('#can').parents('body').bind('touchmove', function block(e) {
				e.preventDefault();
			});
		});
	</r:script>

	<nav:resources override="true" />
</head>
<body>
	<pt:isLoggedIn>

		<div id="logged-in-user">
			<p>Welcome, <b><pt:username /></b> - </p>
			<nav:render group="user" />
		</div>

		<g:formRemote name="searchForm" url="[action: 'index']" update="resultsHolder" after="DocumentSearch.setHash(\$('#q').val())">
		<nav:render group="tabs" />
		<fieldset>
			<div class="right">
				<g:textField name="q" value="${q}" placeholder="Search Documents" maxlength="255" autocapitalize="off" />
				<button id="sub" type="submit" name="submit" class="ui-button icon search"></button>
			</div>
		</fieldset>
		</g:formRemote>

	</pt:isLoggedIn>

	<g:render template="/layouts/messages" />

	<g:layoutBody />

	<r:layoutResources/>

</body>
</html>
