<html>
    <head>
        <meta name="layout" content="main" />
        <title> - Search</title>
        <r:use modules="blueprint-link-icons" />
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
    	<div id="searchResults" class="span-24 last">
    	</div>
    	<r:script>
		$(function() {
			$("#term").focus();
		});
    	</r:script>
    </body>
</html>
                
