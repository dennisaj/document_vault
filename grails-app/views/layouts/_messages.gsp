<r:use module="jquery-ui"/>
<g:if test="${flash.red}">
	<div class="ui-widget">
		<div class="ui-state-error ui-corner-all" style="margin: 20px 0 20px; padding: 0 .7em; ">
			<p style="margin: .7em 0 .7em"><span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em;"></span>
			<strong><g:message code="document-vault.label.alert" />:</strong> ${flash.red}</p>
		</div>
	</div>
</g:if>

<g:if test="${flash.yellow}">
	<div class="ui-widget">
		<div class="ui-state-highlight ui-corner-all" style="margin: 20px 0 20px; padding: 0 .7em;">
			<p style="margin: .7em 0 .7em"><span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
			<strong><g:message code="document-vault.label.info" />:</strong> ${flash.yellow}</p>
		</div>
	</div>
</g:if>
<g:if test="${flash.green}">
	<div class="ui-widget">
		<div class="ui-state-success ui-corner-all" style="margin: 20px 0 20px; padding: 0 .7em;">
			<p style="margin: .7em 0 .7em"><span class="ui-icon ui-icon-circle-check" style="float: left; margin-right: .3em;"></span>
			${flash.green}</p>
		</div>
	</div>
</g:if>
