<div id="tag-search-results">
	<ul>
	<g:each var="tag" in="${tagSearchResults}" status="index">
		<li class="droppable" data-count="${us.paperlesstech.Document.countByTag(tag)}" data-tag="${tag}"><a href="?tagq=tagged ${tag}" onclick="Tagging.showAllTagged('${tag}', '#all-tagged'); return false;">${tag}</a></li>
	</g:each>
	</ul>
</div>
