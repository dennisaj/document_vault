jQuery.extend(nimble.endpoints,{
	delegator: {
		'list':'${createLink(action:'listdelegators')}',
		'search':'${createLink(action:'searchdelegators')}',
		'remove':'${createLink(action:'removedelegator')}',
		'grant':'${createLink(action:'grantdelegator')}'
	}
});

$(function() {
	nimble.listDelegators('${parent.id.encodeAsHTML()}');
	$("#adddelegators").hide();

	$("#showadddelegatorsbtn").click(function () {
		$("#showadddelegators").hide();
		$("#adddelegators").show("blind");
		});

	$("#closedelegatorsearchbtn").click(function () {
		$("#adddelegators").hide();
		$("#showadddelegators").show();
	});
});