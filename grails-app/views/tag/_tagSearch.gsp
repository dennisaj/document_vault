<h3>Tags:</h3>
<g:formRemote name="searchForm" url="[action: 'search']" update="results" asynchronous="false" after="Tagging.initDragAndDrop()">
	<div id="search">
		<label for="q">Search</label>
		<br />
		<input type="text" class="text" name="q" id="q" />
		<input type="submit" name="submit" value="Search" />
	</div>
</g:formRemote>
<g:render template="createTag" />
<div id="results">
	<g:render template="tagSearchResults" />
</div>
<hr />
<div id="allTagged">
</div>
