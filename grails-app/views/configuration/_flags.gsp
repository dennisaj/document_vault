<table id="flagList" class="details">
	<tbody>
	<g:each var="flag" in="${flags}" status="row">
		<tr class="${row % 2 == 0 ? 'even' : 'odd'}">
			<td>${flag}</td>
			<td>
				<g:remoteLink action="removeFlag" params="[flag: flag]" update="flagList"
							  class="button icon icon_delete">
					<g:message code="document-vault.label.remove"/>
				</g:remoteLink>
			</td>
		</tr>
	</g:each>
	</tbody>
</table>
