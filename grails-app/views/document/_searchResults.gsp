<g:set var="haveResults" value="${documents?.results}" />
<div id="searchResults" class="span-24 last">
	<g:if test="${tagSearchResults}">
		<div id="tag-results" class="span-24 last append-bottom">
			<p>Tags</p>
			<g:render template="/tag/tagSearchResults" model="${pageScope.variables}" />
		</div>
		<div id="allTagged">
		</div>
		<g:javascript>Tagging.initDragAndDrop();</g:javascript>
	</g:if>
	<div class="span-24 last append-bottom quiet small">
		<g:if test="${haveResults}">
			Showing <strong> ${documents.offset + 1} </strong> - <strong>
				${documents.results.size() + documents.offset} </strong> of <strong>
				${documents.total} </strong>
			results for <strong> ${queryString} </strong>
		</g:if>
		<g:else>
			No documents found for <strong>${queryString}</strong>.
		</g:else>
		<g:if test="${parseException}">
			<p>
				Your query - <strong>${queryString}</strong> - is not valid.
			</p>
		</g:if>
	</div>
<g:if test="${haveResults}">
	<div class="span-24 last append-bottom">
		<g:each var="it" in="${documents.results}" status="index">
			<div class="result">
				Document: ${it.id}<br />
				Date printed: <g:formatDate date="${it.dateCreated}" format="yyyy-MM-dd hh:mma" /><br />
				<div>
					<ul class="actions">
						<li>
							<a href="${createLink(action: 'show', id: it.id) }">
								<img src="${resource(dir:'images', file:'document-sign.png')}" alt="" /><br />View/Sign
							</a>
						</li>
						<li>
							<a href="${createLink(action: 'download', id: it.id)}">
								<img src="${resource(dir:'images', file:'download.png')}" alt="" /><br />Download
							</a>
						</li>
						<li>
							<a href="javascript:Document.print(${it.id});">
								<img src="${resource(dir:'images', file:'document-print.png')}" alt="" /><br />Print
							</a>
						</li>
						<%--<li>
							<a href="javascript:Document.email(${it.id});">
								<img src="${resource(dir:'images', file:'mail-send.png')}" alt="" /><br />Email
							</a>
						</li>--%>
						<li>
							<a href="javascript:DocumentNote.show('#notebox-${it.id}');">
								<img src="${resource(dir:'images', file:'note.png')}" alt="" /><br />Notes
							</a>
						</li>
						<li>
							<a href="javascript:Tagging.showTagbox('#tagbox-${it.id}');">
								<img src="${resource(dir:'images', file:'list-add.png')}" alt="" /><br />Tags
							</a>
						</li>
					</ul>
					<br /><br /><br />
					<div>
						<ul class="taggable hidden" id="tagbox-${it.id}" documentid="${it.id}"></ul>
					</div>
					<g:if test="${it.signed}">
						<div><g:render template="/saved"><g:message code="document-vault.template.document.signed" /></g:render></div>
					</g:if>
					<a href="${createLink(action: 'show', id: it.id) }">
						<img class="thumb" width="400" src="${createLink(action: 'downloadImage', id: it.id)}" alt="Document ${it.id} Page 1" />
					</a>
					<div class="triangle-border left-arrow hidden" id="notebox-${it.id}">
						<span class="noteField" id="note-${it.id}">${it.searchFields['Note']}</span>
					</div>
				</div>
			</div>
		</g:each>
	</div>

	<div class="paging span-24 last append-bottom">
		Page:
		<g:set var="totalPages"
			value="${Math.ceil(documents.total / documents.max)}" />
		<g:if test="${totalPages == 1}">
			<span class="currentStep">1</span>
		</g:if>
		<g:else>
			<g:paginate controller="searchable" action="index"
				params="[q: queryString]" total="${documents.total}"
				prev="&lt; previous" next="next &gt;" />
		</g:else>
	</div>
</g:if>
</div>