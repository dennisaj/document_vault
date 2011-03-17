<div>
	<g:if test="${!documents}">
		No results found.
	</g:if>
	<g:else>
		<div class="span-24 last append-bottom quiet small">
			${documents.size()} document<g:if test="${documents.size() > 1}">s</g:if> found.
		</div>
		<g:each in="${documents}">
			<div class="span-24 last append-bottom">
			    RO Number: ${it.roNumber}<br />
				Customer Name: ${it.customerName}<br />
				Download: <a href="${createLink(action: 'downloadPdf', id: it.id)}">Download</a>
			</div>
		</g:each>
	</g:else>
</div>