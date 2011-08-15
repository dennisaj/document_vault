<%@page import="us.paperlesstech.nimble.Group"%>
<%@page import="us.paperlesstech.DocumentPermission"%>
<jq:jquery>
nimble.createPermission = function(ownerID) {
	var dataString = 'id=' + ownerID + '&first=' + $('#first_p').val() + '&second=' + $('#second_p').val() + '&third=' + $('#third_p').val() + '&fourth=' + $('#fourth_p').val();
	$.ajax({
		type: "POST",
		url: nimble.endpoints.permission.create,
		data: dataString,
		success: function(res) {
			$("#addpermissionserror").empty();
			nimble.listPermissions(ownerID);
			nimble.growl('success', res);
		},
		error: function (xhr) {
			$("#addpermissionserror").empty().append(xhr.responseText)
		}
	});
};
</jq:jquery>
<div id="permissions" class="section">
	<h3><g:message code="nimble.template.permissions.heading" /></h3>
	<div id="currentpermission">
	</div>

	<div id="showaddpermissions">
		<a id="showaddpermissionsbtn" class="button icon icon_group_add"><g:message code="nimble.link.addpermission" /></a>
	</div>

	<div id="addpermissions">
		<h4><g:message code="nimble.template.permission.add.heading" /></h4>
		<p>
			<g:message code="nimble.template.permission.add.descriptive" />
		</p>

		<div id="addpermissionserror"></div>
		<table>
			<tbody>
			<tr>
				<td>
					<g:select name="third_p" class="easyinput" from="${Group.list()}" optionKey="id" 
							optionValue="${{message(code:'document-vault.label.groupdropdown', args:[it.name])}}" 
							value="${parent instanceof Group ? parent.id : ''}" 
							noSelection="['*':message(code:'document-vault.label.allgroups')]" />
					<strong>:</strong>
					<g:select name="second_p" class="easyinput" from="${DocumentPermission.values()}" value="${DocumentPermission.View.name().toLowerCase()}"
							optionKey="${{it.name().toLowerCase()}}" 
							valueMessagePrefix="document-vault.label.documentpermission" />
					<g:hiddenField name="first_p" value="document" />
					<g:hiddenField name="fourth_p" value="*" />
				</td>
			</tr>
			</tbody>
		</table>
		<button onClick="nimble.createPermission(${parent.id.encodeAsHTML()});" class="button icon icon_add"><g:message code="nimble.link.createpermission" /></button>
		<button id="closepermissionsaddbtn" class="button icon icon_cross"><g:message code="nimble.link.close" /></button>
	</div>
</div>
