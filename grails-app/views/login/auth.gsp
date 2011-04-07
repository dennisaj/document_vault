<html>
<head>
<meta name='layout' content='main' />
<title> - Login</title>
</head>

<body>
	<div id='leftColumn' class='span-8'> &nbsp;</div>
	<div id='login' class='span-8'>
		<form action='${postUrl}' method='POST' id='loginForm' autocomplete='off'>
		<fieldset>
			<legend>Login</legend>
			<p>
				<label for='username'>User Name</label><br />
				<input type='text' class='text' name='j_username' id='j_username' autocapitalize='off' />
			</p>
			<p>
				<label for='password'>Password</label>
				<input type='password' class='text' name='j_password' id='j_password' />
			</p>
			<p>
				<label for='remember_me'>Remember me</label><br />
				<input type='checkbox' class='chk' name='${rememberMeParameter}' id='remember_me'
				<g:if test='${hasCookie}'>checked='checked'</g:if> />
			</p>
			<p class="prepend-top">
				<input type='submit' value='Login' /> 
				<input type='reset' value='Clear' /> 
			</p>
		</fieldset>
		</form>
	</div>
	<div id='rightColumn' class='span-8 last'> &nbsp;</div>
	<jq:jquery>
		$("#j_username").focus();
	</jq:jquery>
</body>
</html>
