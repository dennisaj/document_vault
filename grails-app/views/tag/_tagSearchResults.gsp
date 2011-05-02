<div id="tag-search-results">
<ul>
<g:each var="tag" in="${tagSearchResults}" status="index">
	<li><a href="javascript:Tagging.showAllTagged('${tag}', '#allTagged');"><span class="droppable" tag="${tag}"></span>${tag}</a></li>
</g:each>
</ul>
</div>
