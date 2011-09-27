<div id="search-results">
	<g:formRemote name="tag-search-form" url="[action: 'search']" update="tag-results" asynchronous="false" after="Tagging.initTagResults()">
		<fieldset>
				<input type="text" class="text" name="tagq" id="tagq" placeholder="<g:message code="document-vault.view.tag.search.title" />" />
		</fieldset>
	</g:formRemote>
	
	<g:formRemote name="tag-create-form" url="[action: 'create']" update="status" asynchronous="false">
		<fieldset>
			<input type="text" class="text" name="name" id="tagName" placeholder="<g:message code="document-vault.label.newtag" /> " /><span id="status"></span>
		</fieldset>
	</g:formRemote>
	
	<g:if test="${!params.tagq && !tagSearchResults}">
		<g:message code="document-vault.view.tag.search.noresults" />
	</g:if>
	<%--<g:if test="${!params.tagqq && tagSearchResults}">
		<g:message code="document-vault.view.tag.search.recent" />
	</g:if>--%>
	<g:if test="${params.tagq && tagSearchResults}">
		<g:message code="document-vault.view.tag.search.results" args="[params.tagq.encodeAsHTML()]" />
	</g:if>
	<g:if test="${params.tagq && !tagSearchResults}">
		<g:message code="document-vault.view.tag.search.noresultsforsearch" args="[params.tagq.encodeAsHTML()]" />
	</g:if>
</div>

<%--<div id="tag-actions">

	
	<g:formRemote name="tag-search-form" url="[action: 'search']" update="tag-results" asynchronous="false" after="Tagging.initDragAndDrop()">
		<fieldset>
				<input type="text" class="text" name="tagq" id="tagq" placeholder="<g:message code="document-vault.view.tag.search.title" />" />
				<button type="submit" name="submit" id="tag-search-submit" class="icon search"></button>
		</fieldset>
	</g:formRemote>
</div>--%>

<div id="tag-results">
	<g:render template="tagSearchResults" />
</div>
