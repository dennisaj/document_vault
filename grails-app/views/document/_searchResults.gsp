<div id="search-results">
	<g:if test="${!params.q && !documentResults}">
		<g:message code="document-vault.view.document.search.noresults" />
	</g:if>
	<g:if test="${!params.q && documentResults}">
		<g:message code="document-vault.view.document.search.recent" args="[params.max.encodeAsHTML()]" />
	</g:if>
	<g:if test="${params.q && documentResults}">
		<g:message code="document-vault.view.document.search.results" args="[params.q.encodeAsHTML()]" />
	</g:if>
	<g:if test="${params.q && !documentResults}">
		<g:message code="document-vault.view.document.search.noresultsforsearch" args="[params.q.encodeAsHTML()]" />
	</g:if>
</div>

<g:if test="${tagSearchResults && pt.canTagAny()}">
<div id="tag-results">
	<g:render template="/tag/tagSearchResults" model="${pageScope.variables}" />
</div>
</g:if>

<g:if test="${documentResults}">
<ul id="document-items">
	<g:each var="document" in="${documentResults}" status="index">

	<li class="document <g:if test="${document.signed()}">signed</g:if>">

		<g:if test="${document.previewImage(1)}">
		<a class="thumb" href="${createLink(action:'downloadImage', params:[documentId: document.id, pageNumber:1])}">
			<img src="${createLink(action:'thumbnail', params:[documentId: document.id, pageNumber:1, documentDataId:document.previewImage(1).thumbnail.id])}" alt="Document ${document.id} Page 1" title="<g:message code="document-vault.label.clicktopreview" />" />
		</a>
		</g:if>

		<p>
			<b>${document.toString()}</b><br />
			<span title="${document.dateCreated}"><g:message code="document-vault.label.added" /> <prettytime:display date="${document.dateCreated}" /> -</span>
			<span><prettysize:display size="${document?.files?.first()?.fileSize}" abbr="true" format="###0" /></span>
		</p>

		<div class="ui-button-group document-actions">
			<pt:canTag document="${document}">
			<a href="#" class="ui-button tags" title="<g:message code="document-vault.label.tags" />" onclick="javascript:return Tagging.showTagbox('#tagbox-${document.id}', this);">
				<g:message code="document-vault.label.tags" />
			</a>
			</pt:canTag>

			<pt:canNotes document="${document}">
			<a id="notes-button-${document.id}" href="#" class="ui-button notes" title="<g:message code="document-vault.label.notes" />" onclick="javascript:return DocumentNote.show('#notebox-${document.id}', this);" data-count="${document.notes.size()?:""}">
				<g:message code="document-vault.label.notes" />
			</a>
			</pt:canNotes>

			<pt:canPrint document="${document}">
			<a href="#" class="ui-button print" title="<g:message code="document-vault.label.print" />" onclick="javascript:Document.print(${document.id});">
				<g:message code="document-vault.label.print" />
			</a>
			</pt:canPrint>

			<a href="${createLink(action: 'show', params:[documentId: document.id])}" class="ui-button show" title="<g:message code="document-vault.label.view" />">
				<g:message code="document-vault.label.view" />
			</a>

			<a href="${createLink(action: 'download', params:[documentId: document.id, documentDataId: document.files.first().id])}" class="ui-button download" title="<g:message code="document-vault.label.download" />">
				<g:message code="document-vault.label.download" />
			</a>

			<pt:canSign document="${document}">
			<a href="${createLink(action: 'sign', params:[documentId: document.id])}" class="ui-button sign" title="<g:message code="document-vault.label.sign" />">
				<g:message code="document-vault.label.sign" />
			</a>
			</pt:canSign>
		</div>

		<pt:canNotes document="${document}">
		<div id="notebox-${document.id}" class="document-meta hidden">
			<div id="noteField-${document.id}" class="notebox-items">
				<g:render template="/note/textNotes" model="[document:document]" />
			</div>
			<g:form name="noteForm-${document.id}">
				<g:textArea class="noteTextarea" name="value" id="note-${document.id}" rows="5" />
				<g:submitToRemote url="[controller:'note', action:'saveText', params:[documentId:document.id]]" name="submit" 
					value="${message(code:'document-vault.label.submit')}" 
					update="noteField-${document.id}"
					after="\$('#note-${document.id}').val('');"
					onSuccess="\$('#notes-button-${document.id}').attr('data-count', \$.map(\$(data), function(val) {return \$(val).is('p') ? val:null;}).length)"
					class="ui-button" />
			</g:form>
		</div>
		</pt:canNotes>

		<pt:canTag document="${document}">
		<div class="document-meta hidden">
			<ul class="taggable" id="tagbox-${document.id}" data-documentid="${document.id}"></ul>
		</div>
		</pt:canTag>

	</li>
	</g:each>
</ul>

<div class="pagination">
	<g:paginate params="${params}" total="${documentTotal}" />
</div>

</g:if>
