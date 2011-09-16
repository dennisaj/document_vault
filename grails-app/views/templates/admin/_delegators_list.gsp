<g:if test="${delegators?.size() > 0}">
	<table class="details">
		<thead>
		<tr>
			<th class="first"><g:message code="nimble.label.username" /></th>
			<th class=""><g:message code="nimble.label.name" /></th>
			<th class="last"></th>
		</tr>
		</thead>
		<tbody>
		<g:each in="${delegators}" status="i" var="delegator">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td>${delegator.username?.encodeAsHTML()}</td>
				<td>${delegator.profile.fullName?.encodeAsHTML()}</td>
				<td>
					<g:link controller="user" action="show" id="${delegator.id.encodeAsHTML()}" class="button icon icon_user_go"><g:message code="nimble.link.view" /></g:link>
					<a onClick="nimble.removeDelegator('${ownerID.encodeAsHTML()}', '${delegator.id.encodeAsHTML()}');" class="button icon icon_delete"><g:message code="nimble.link.remove" /></a>
				</td>
			</tr>
		</g:each>
		</tbody>
	</table>
</g:if>
<g:else>
	<p>
		<g:message code="nimble.template.delegators.list.noresults" />
	</p>
</g:else>
