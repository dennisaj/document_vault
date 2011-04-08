<g:if test="${documentType && documentType.searchOptions}">
	<g:each in="${documentType.searchOptions}">
		<label for="field_${it}">${it.replaceAll("_", " ")}</label><br />
		<g:textField name="field_${it}"></g:textField><br />
	</g:each>
</g:if>
<g:if test="${documentType && !documentType.searchOptions}">
	No additional search options available for this document type.
</g:if>
<g:else>
	&nbsp;
</g:else>