<div id="delegators" class="section">
	<h3><g:message code="nimble.template.delegators.heading" /></h3>

	<div id="assigneddelegators">
	</div>

	<div id="showadddelegators">
		<a id="showadddelegatorsbtn" class="button icon icon_group_add"><g:message code="nimble.link.adddelegators" /></a>
	</div>

	<div id="adddelegators">
		<h4><g:message code="nimble.template.delegators.add.heading" /></h4>
		<p>
			<g:message code="nimble.template.delegators.add.descriptive" />
		</p>

		<div class="searchbox">
			<g:textField name="qdelegators" class="enhancedinput"/>
			<button onClick="nimble.searchDelegators('${parent.id.encodeAsHTML()}');" class="button icon icon_magnifier"><g:message code="nimble.link.search" /></button>
			<button id="closedelegatorsearchbtn" class="button icon icon_cross"><g:message code="nimble.link.close" /></button>
		</div>

		<div id="delegatorsearchresponse" class="clear">
		</div>
	</div>
</div>
