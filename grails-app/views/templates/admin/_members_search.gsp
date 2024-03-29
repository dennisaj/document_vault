<g:if test="${users != null && users.size() > 0}">
  <table class="details">
    <thead>
    <tr>
      <th class="first"><g:message code="nimble.label.username" /></th>
      <th class=""><g:message code="nimble.label.fullname" /></th>
      <th class="last"></th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${users}" status="i" var="user">
      <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
        <g:if test="${user.username.length() > 30}">
        	<td>${user.username?.substring(0,30).encodeAsHTML()}...</td>
		</g:if>
		<g:else>
			<td>${user.username?.encodeAsHTML()}</td>
		</g:else>
        <td>${user.profile?.fullName?.encodeAsHTML()}</td>
        <td>
          <g:link controller="user" action="show" id="${user.id.encodeAsHTML()}" class="button icon icon_user_go"><g:message code="nimble.link.view" /></g:link>
          <a onClick="nimble.addMember('${parent.id.encodeAsHTML()}', '${user.id.encodeAsHTML()}', '${user.username.encodeAsHTML()}');" class="button icon icon_add"><g:message code="nimble.link.grant" /></a>
        </td>
      </tr>
    </g:each>
    </tbody>
  </table>
</g:if>
<g:else>
  <div class="info">
  <strong><g:message code="nimble.template.members.add.user.noresults" /></strong>
  </div>
</g:else>