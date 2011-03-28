<div id="searchResults" class="span-24 last">
	<g:if test="${!documents}">
		No results found.
	</g:if>
	<g:else>
		<div class="span-24 last append-bottom quiet small">
			${documents.size()} document<g:if test="${documents.size() > 1}">s</g:if> found.
		</div>
		<g:each in="${documents}">
			<div class="span-24 last append-bottom">
			    Document: ${it.id}<br />
			    <a href="${createLink(action: 'show', id: it.id) }"><img width="400" src="${createLink(action: 'downloadImage', id: it.id) }" alt="Document ${it.id} Page 1"/></a><br />
				Download: <a href="${createLink(action: 'downloadPdf', id: it.id)}">Download</a>
			</div>
		</g:each>
	</g:else>
</div>