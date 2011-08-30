<g:formRemote name="tag-search-form" url="[action: 'search']" update="tag-results" asynchronous="false" after="Tagging.initDragAndDrop()">
	<fieldset>
		<div id="search" class="left">
			<input type="text" class="text" name="tagq" id="tagq" placeholder="<g:message code="document-vault.view.tag.search.title" />" />
			<button type="submit" name="submit" id="tag-search-submit" class="icon search"></button>
		</div>
	</fieldset>
</g:formRemote>
<g:render template="createTag" />
<div id="tag-results">
	<g:render template="tagSearchResults" />
</div>
<div id="allTagged">
</div>
