<div id="request-signature">
	<div id="party-container">
		<g:render template="/party/parties" model="model" />
	</div>
	<pt:canGetSigned document="${document}">
	<div id="add-party-container">
		<button id="add-party" title="<g:message code="document-vault.view.signature.addparty" />" onclick="${remoteFunction(controller:'party', action:'addParty', params:[documentId: document.id], onFailure: 'Document.ajaxErrorHandler(XMLHttpRequest, textStatus, errorThrown)', onSuccess:'Party.addParty(data)')}">
			<g:message code="document-vault.view.signature.addparty" />
		</button>
		<button id="submit-parties" title="<g:message code="document-vault.view.signature.submitparties" />">
			<g:message code="document-vault.view.signature.submitparties" />
		</button>
	</div>
	</pt:canGetSigned>
</div>
