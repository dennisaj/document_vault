<%@page import="us.paperlesstech.nimble.Group"%>
<%@page import="us.paperlesstech.DocumentPermission"%>
<%@page import="us.paperlesstech.BucketPermission"%>
<%@page import="us.paperlesstech.Bucket"%>
<jq:jquery>
nimble.createGroupPermission = function(ownerID) {
	nimble.createPermission(ownerID, 'group_');
};

nimble.createBucketPermission = function(ownerID) {
	nimble.createPermission(ownerID, 'bucket_');
};

nimble.createPermission = function(ownerID, prefix) {
	var dataString = 'id=' + ownerID + '&first=' + $('#' + prefix + 'first_p').val() + '&second=' + $('#' + prefix + 'second_p').val() + '&third=' + $('#' + prefix + 'third_p').val() + '&fourth=' + $('#' + prefix + 'fourth_p').val();
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
		<g:select name="group_third_p" class="easyinput" from="${Group.list()}" optionKey="id" 
				optionValue="${{message(code:'document-vault.label.groupdropdown', args:[it.name])}}"
				value="${parent instanceof Group ? parent.id : ''}"
				noSelection="['*':message(code:'document-vault.label.allgroups')]" />
		<strong>:</strong>
		<g:select name="group_second_p" class="easyinput" from="${DocumentPermission.values()}" value="${DocumentPermission.View.name().toLowerCase()}"
				optionKey="${{it.name().toLowerCase()}}" 
				valueMessagePrefix="document-vault.label.documentpermission" />
		<g:hiddenField name="group_first_p" value="document" />
		<g:hiddenField name="group_fourth_p" value="*" />
		<button onClick="nimble.createGroupPermission(${parent.id.encodeAsHTML()});" class="button icon icon_add"><g:message code="nimble.link.createpermission" /></button>

		<br />
		<g:select name="bucket_third_p" class="easyinput" from="${Bucket.list()}" optionKey="id"
				optionValue="${{message(code:'document-vault.label.bucketdropdown', args:[it.name])}}"
				value="${parent instanceof Bucket ? parent.id : ''}"
				noSelection="['*':message(code:'document-vault.label.allbuckets')]" />
		<strong>:</strong>
		<g:select name="bucket_second_p" class="easyinput" from="${BucketPermission.values()}" value="${BucketPermission.View.name().toLowerCase()}"
				optionKey="${{it.name().toLowerCase()}}"
				valueMessagePrefix="document-vault.label.bucketpermission" />
		<g:hiddenField name="bucket_first_p" value="bucket" />
		<g:hiddenField name="bucket_fourth_p" value="*" />
		<button onClick="nimble.createBucketPermission(${parent.id.encodeAsHTML()});" class="button icon icon_add"><g:message code="nimble.link.createpermission" /></button>

		<br />
		<button id="closepermissionsaddbtn" class="button icon icon_cross"><g:message code="nimble.link.close" /></button>
	</div>
</div>
