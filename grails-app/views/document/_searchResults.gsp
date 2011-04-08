<g:set var="haveResults" value="${searchResult?.results}" />
<div id="searchResults" class="span-24 last">
	<div class="span-24 last append-bottom quiet small">
		<g:if test="${haveResults}">
      		Showing <strong> ${searchResult.offset + 1} </strong> - <strong>
				${searchResult.results.size() + searchResult.offset} </strong> of <strong>
				${searchResult.total} </strong>
       		results for <strong> ${queryString} </strong>
		</g:if>
		<g:else>
			No results found for <strong>${queryString}</strong>.
       	</g:else>
		<g:if test="${parseException}">
			<p>
				Your query - <strong>${queryString}</strong> - is not valid.
			</p>
		</g:if>
	</div>
<g:if test="${haveResults}">
	<div class="span-24 last append-bottom">
		<g:each var="it" in="${searchResult.results}" status="index">
			<div class="result">
				Document: ${it.id}<br />
				Date printed: ${it.dateCreated.toString("yyyy-MM-dd hh:mma")}<br />
				<a href="${createLink(action: 'show', id: it.id) }"><img
					width="400"
					src="${createLink(action: 'downloadImage', id: it.id) }"
					alt="Document ${it.id} Page 1" /> </a><br /> Download: <a
					href="${createLink(action: 'downloadPdf', id: it.id)}">Download</a>
			</div>
		</g:each>
	</div>

	<div class="paging span-24 last append-bottom">
		Page:
		<g:set var="totalPages"
			value="${Math.ceil(searchResult.total / searchResult.max)}" />
		<g:if test="${totalPages == 1}">
			<span class="currentStep">1</span>
		</g:if>
		<g:else>
			<g:paginate controller="searchable" action="index"
				params="[q: queryString]" total="${searchResult.total}"
				prev="&lt; previous" next="next &gt;" />
		</g:else>
	</div>
</g:if>
</div>