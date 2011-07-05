<h3><g:message code="document-vault.label.tags" />:</h3>
<g:formRemote name="searchForm" url="[action: 'search']" update="tag-results" asynchronous="false" after="Tagging.initDragAndDrop()">
	<div id="search">
		<label for="q"><g:message code="document-vault.label.search" /></label>
		<br />
		<input type="text" class="text" name="q" id="q" />
		<button type="submit" name="submit" id="tag-search-submit">
			<g:message code="document-vault.label.search" />
		</button>
	</div>
</g:formRemote>
<g:render template="createTag" />
<div id="tag-results">
	<g:render template="tagSearchResults" />
</div>
<hr />
<div id="allTagged">
</div>
