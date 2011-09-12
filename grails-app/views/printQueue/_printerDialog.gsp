<div id="printer-select" title="<g:message code="document-vault.view.print.title" />">
	<p>
		<input type="hidden" id="print-documentId" value="${document.id}" />
		<g:select name="printer" from="${us.paperlesstech.Printer.list()}" optionKey="id" value="${defaultPrinter}" />
		<pt:canNotes document="${document}"><br /><g:checkBox name="addNotes" id="addNotes" /><g:message code="document-vault.view.print.includenotes" /></pt:canNotes>
	</p>
</div>
