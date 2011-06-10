var DocumentNote = {
	urls: {},

	editableOptions: function() {
		return {
			cancel: 'Cancel',
			cssclass: 'noteTextarea',
			id: 'documentId',
			indicator: '<img src="{0}">'.format(this.urls.spinner),
			loadurl: this.urls.load,
			method: 'POST',
			onblur: 'ignore',
			placeholder: 'Click here to add a note.',
			rows: 5,
			submit: 'OK',
			tooltip: 'Click to add/edit a note...',
			type: 'textarea'
		}
	},

	show: function(id, button) {
		var self = this;
		if (!$('.noteField', id).data('event.editable')) {
			// If the noteField is not editable, make it editable
			$('.noteField', id).editable(self.urls.save, this.editableOptions());
		}

		$(button).toggleClass('ui-state-active');
		$(id).toggleClass('hidden');
	},

	init: function(urls) {
		this.urls = urls || this.urls;
		$('.noteField').editable(this.urls.save, this.editableOptions());
	}
};
