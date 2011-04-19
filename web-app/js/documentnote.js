var DocumentNote = {
	editableOptions: {
		cancel: 'Cancel',
		cssclass: 'noteTextarea',
		indicator: '<img src="/document_vault/images/spinner.gif">',
		method: 'POST',
		onblur: 'ignore',
		placeholder: 'Click here to add a note.',
		rows: 5,
		submit: 'OK',
		tooltip: 'Click to add/edit a note...',
		type: 'textarea'
	},
	
	urls: {},

	show: function(id) {
		if (!$('.noteField', id).data('event.editable')) {
			// If the noteField is not editable, make it editable
			$('.noteField', id).editable(DocumentNote.urls['save'], DocumentNote.editableOptions);
		}
		$(id).toggleClass('hidden');
	},

	init: function(urls) {
		DocumentNote.urls = urls || DocumentNote.urls;
		$('.noteField').editable(DocumentNote.urls['save'], DocumentNote.editableOptions);
	}
};
