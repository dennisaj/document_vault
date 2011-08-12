<%@page import="us.paperlesstech.MimeType"%>
<div id="searchResults" class="span-24 last">
	<g:if test="${tagSearchResults}">
		<div id="tag-results" class="span-24 last append-bottom">
			<p>Tags</p>
			<g:render template="/tag/tagSearchResults" model="${pageScope.variables}"/>
		</div>
	</g:if>
	<div class="span-24 last ui-widget-header ui-corner-top">
		<g:if test="${!params.q}">
			<g:message code="document-vault.view.document.search.recent" args="[params.max.encodeAsHTML()]" />
		</g:if>
		<g:if test="${params.q && documentResults}">
			<g:message code="document-vault.view.document.search.results" args="[params.q.encodeAsHTML()]" />
		</g:if>
		<g:if test="${params.q && !documentResults}">
			<g:message code="document-vault.view.document.search.noresults" args="[params.q.encodeAsHTML()]" />
		</g:if>
	</div>
	<g:if test="${documentResults}">
	<div class="span-24 last ui-widget-content ui-corner-bottom">
		<table id="document-table">
		<g:each var="document" in="${documentResults}" status="index">
			<tr class="result">
				<td>
					<%-- The previewImage will not be available if the conversion is still in progress. --%>
					<g:if test="${document.previewImage(1)}">
					<a class="thumb" href="${createLink(action:'downloadImage', params:[documentId: document.id, pageNumber:1])}">
						<img src="${createLink(action:'thumbnail', params:[documentId: document.id, pageNumber:1, documentDataId:document.previewImage(1).thumbnail.id])}" alt="Document ${document.id} Page 1" title="<g:message code="document-vault.label.clicktopreview" />" />
					</a>
					</g:if>
				</td>
				<td>
					${document.toString()}
					<g:if test="${document.signed()}">
						<span class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only" style="cursor: default" title="<g:message code="document-vault.label.signed" />">
							<span class="ui-icon ui-icon-pencil"></span>
							<span class="ui-button-text"><g:message code="document-vault.label.signed" /></span>
						</span>
					</g:if>
				</td>
				<td>
					<span title="${document.dateCreated}"><g:message code="document-vault.label.added" /> <prettytime:display date="${document.dateCreated}" /></span>
				</td>
				<td>
					<prettysize:display size="${document?.files?.first()?.fileSize}" abbr="true" format="###0" />
				</td>
				<td>
					<pt:canTag document="${document}">
					<button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only" title="<g:message code="document-vault.label.tags" />" onclick="javascript:Tagging.showTagbox('#tagbox-${document.id}', this);">
						<span class="ui-button-icon-primary ui-icon ui-icon-tag"></span>
						<span class="ui-button-text"><g:message code="document-vault.label.tags" /></span>
					</button>
					</pt:canTag>
					<pt:canNotes document="${document}">
					<button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only" title="<g:message code="document-vault.label.notes" />" onclick="javascript:DocumentNote.show('#notebox-${document.id}', this);">
						<span class="ui-button-icon-primary ui-icon ui-icon-note"></span>
						<span class="ui-button-text"><g:message code="document-vault.label.notes" /></span>
					</button>
					</pt:canNotes>
					<pt:canPrint document="${document}">
					<button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only" title="<g:message code="document-vault.label.print" />" onclick="javascript:Document.print(${document.id});">
						<span class="ui-button-icon-primary ui-icon ui-icon-print"></span>
						<span class="ui-button-text"><g:message code="document-vault.label.print" /></span>
					</button>
					</pt:canPrint>
					<a href="${createLink(action: 'show', params:[documentId: document.id])}" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only" title="<g:message code="document-vault.label.view" />" style="text-decoration: none">
						<span class="ui-button-icon-primary ui-icon ui-icon-zoomin"></span>
						<span class="ui-button-text"><g:message code="document-vault.label.view" /></span>
					</a>
					<a href="${createLink(action: 'download', params:[documentId: document.id, documentDataId: document.files.first().id])}" style="text-decoration: none" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only" title="<g:message code="document-vault.label.download" />">
						<span class="ui-button-icon-primary ui-icon ui-icon-circle-arrow-s"></span>
						<span class="ui-button-text"><g:message code="document-vault.label.download" /></span>
					</a>
					<pt:canSign document="${document}">
					<a href="${createLink(action: 'sign', params:[documentId: document.id])}" style="text-decoration: none" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only" title="<g:message code="document-vault.label.sign" />">
						<span class="ui-button-icon-primary ui-icon ui-icon-circle-arrow-e"></span>
						<span class="ui-button-text"><g:message code="document-vault.label.sign" /></span>
					</a>
					</pt:canSign>
				</td>
			</tr>
			<pt:canNotes document="${document}">
			<tr id="notebox-${document.id}" class="hidden">
				<td>
					<g:message code="document-vault.label.notes" />:
				</td>
				<td colspan="4">
					<span class="noteField" id="${document.id}">${document.searchField('Note')}</span>
				</td>
			</tr>
			</pt:canNotes>
			<pt:canTag document="${document}">
			<tr class="hidden">
				<td>
					<g:message code="document-vault.label.tags" />:
				</td>
				<td colspan="4">
					<ul class="taggable" id="tagbox-${document.id}" documentid="${document.id}"></ul>
				</td>
			</tr>
			</pt:canTag>
		</g:each>
		</table>
	</div>

	<div class="paginateButtons paging span-24 last append-bottom">
		<g:paginate params="${params}" total="${documentTotal}" />
	</div>
	</g:if>
</div>
