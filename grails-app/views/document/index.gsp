<html>
    <head>
        <meta name="layout" content="main" />
        <title> - Search</title>
    </head>
    <body>
    	<g:formRemote name="searchForm" url="[action: 'search']" update="searchResults">
    	<fieldset class="span-24 last">
    		<legend>Search for a document</legend>
    		<p>
    			<label for="term">RO Number</label><br />
    			<input type="text" class="text" name="term" id="term" />
    		</p>
    	</fieldset>
    	</g:formRemote>
    	<g:render template="searchResults" />
		<jq:jquery>
			$("#term").focus();
    	</jq:jquery>
    </body>
</html>
                
