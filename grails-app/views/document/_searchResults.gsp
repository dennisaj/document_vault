<%@page import="us.paperlesstech.MimeType"%>
<div id="searchResults" class="span-24 last">
	<g:if test="${tagSearchResults}">
		<div id="tag-results" class="span-24 last append-bottom">
			<p>Tags</p>
			<g:render template="/tag/tagSearchResults" model="${pageScope.variables}"/>
		</div>
	</g:if>
	<div class="span-24 last quiet ui-widget-header ui-corner-top">
		<g:if test="${!q}">
			Showing ${max.encodeAsHTML()} most recent documents.
		</g:if>
		<g:if test="${q && documents}">
			Showing results for <strong>${q.encodeAsHTML()}</strong>
		</g:if>
		<g:if test="${q && !documents}">
			No documents found for <strong>${q.encodeAsHTML()}</strong>.
		</g:if>
	</div>
	<g:if test="${documents}">
	<div class="span-24 last ui-widget-content ui-corner-bottom">
		<table id="document-table">
		<g:each var="it" in="${documents}" status="index">
			<tr class="result">
				<td>
					<img class="thumb" width="80" src="${createLink(action:'downloadImage', params:[documentId: it.id])}" alt="Document ${it.id} Page 1" title="<g:message code="document-vault.label.clicktopreview" />" />
				</td>
				<td>
					${it.toString()?.encodeAsHTML()}
					<g:if test="${it.signed}">
						<span class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only" style="cursor: default" title="<g:message code="document-vault.label.signed" />">
							<span class="ui-icon ui-icon-pencil"></span>
							<span class="ui-button-text"><g:message code="document-vault.label.signed" /></span>
						</span>
					</g:if>
				</td>
				<td>
					<span title="${it.dateCreated}"><g:message code="document-vault.label.added" /> <prettytime:display date="${it.dateCreated}" /></span>
				</td>
				<td>
					<prettysize:display size="${it?.files?.first()?.data?.size()}" abbr="true" format="###0" />
				</td>
				<td>
					<button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only" title="<g:message code="document-vault.label.tags" />" onclick="javascript:Tagging.showTagbox('#tagbox-${it.id}', this);">
						<span class="ui-button-icon-primary ui-icon ui-icon-tag"></span>
						<span class="ui-button-text"><g:message code="document-vault.label.tags" /></span>
					</button>
					<button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only" title="<g:message code="document-vault.label.notes" />" onclick="javascript:DocumentNote.show('#notebox-${it.id}', this);">
						<span class="ui-button-icon-primary ui-icon ui-icon-note"></span>
						<span class="ui-button-text"><g:message code="document-vault.label.notes" /></span>
					</button>
					<g:if test="${it.files.first().mimeType == MimeType.PDF }">
					<button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only" title="<g:message code="document-vault.label.print" />" onclick="javascript:Document.print(${it.id});">
						<span class="ui-button-icon-primary ui-icon ui-icon-print"></span>
						<span class="ui-button-text"><g:message code="document-vault.label.print" /></span>
					</button>
					</g:if>
					<a href="${createLink(action: 'download', params:[documentId: it.id])}">
						<button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only" title="<g:message code="document-vault.label.download" />">
							<span class="ui-button-icon-primary ui-icon ui-icon-circle-arrow-s"></span>
							<span class="ui-button-text"><g:message code="document-vault.label.download" /></span>
						</button></a>
					<a href="${createLink(action: 'sign', params:[documentId: it.id])}">
						<button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only" title="<g:message code="document-vault.label.view" />">
							<span class="ui-button-icon-primary ui-icon ui-icon-circle-arrow-e"></span>
							<span class="ui-button-text"><g:message code="document-vault.label.view" /></span>
						</button></a>
				</td>
			</tr>
			<tr id="notebox-${it.id}" class="hidden">
				<td>
					<g:message code="document-vault.label.notes" />:
				</td>
				<td colspan="4">
					<span class="noteField" id="${it.id}">${it.searchField('Note')}</span>
				</td>
			</tr>
			<tr class="hidden">
				<td>
					<g:message code="document-vault.label.tags" />:
				</td>
				<td colspan="4">
					<ul class="taggable" id="tagbox-${it.id}" documentid="${it.id}"></ul>
				</td>
			</tr>
		</g:each>
		</table>
	</div>

	<div class="paging span-24 last append-bottom">
		Page: 1
		<%--
		<g:set var="totalPages" value="${Math.ceil(documents.total / documents.max)}"/>
		<g:if test="${totalPages == 1}">
			<span class="currentStep">1</span>
		</g:if>
		<g:else>
			<g:paginate controller="searchable" action="index"
						params="[q: queryString]" total="${documents.total}"
						prev="&lt; previous" next="next &gt;"/>
		</g:else>
		--%>
	</div>
	</g:if>
</div>