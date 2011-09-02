<div id="tag-search-results">
<ul>
<g:each var="tag" in="${tagSearchResults}" status="index">
	<li><a href="?tagq=tagged ${tag}" onclick="Tagging.showAllTagged('${tag}', '#allTagged'); return false;"><span class="droppable" data-tag="${tag}"></span>${tag}</a></li>
</g:each>
</ul>
</div>
