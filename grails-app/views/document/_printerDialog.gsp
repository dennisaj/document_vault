<div id="printer-select" title="Choose a Printer" class="hidden">
	<p>
		<span class="ui-icon ui-icon-print" style="float: left; margin: 0 7px 50px 0;"></span>
		<input type="hidden" id="print-documentId" value="" />
		<g:select name="printer" from="${us.paperlesstech.Printer.list()}" optionKey="id" />
	</p>
</div>
