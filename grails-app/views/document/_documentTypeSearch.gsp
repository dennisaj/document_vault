<g:if test="${documentType?.searchOptions}">
	<g:each in="${documentType.searchOptions}">
		<label for="field_${it}">${it.replaceAll("_", " ")}</label><br />
		<g:textField name="field_${it}"></g:textField><br />
	</g:each>
</g:if>
<g:if test="${documentType && !documentType.searchOptions}">
	<label for="field_raw">Search only this document type</label><br />
	<g:textField name="field_raw" />
</g:if>
<g:else>
	&nbsp;
</g:else>