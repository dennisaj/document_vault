var Document = {
	ajaxErrorHandler: function() {},
	urls: {},

	_email: function(documentId, email) {
		var self = this;
		$.ajax({
			beforeSend: function() {/* Add throbber */ },
			complete: function() {/* Remove throbber */ },
			error: this.ajaxErrorHandler,
			global: false,
			success: function(data) {
				if (data.status == 'success') {
					HtmlAlert._alert('Email was sent', '<p><span class="ui-icon ui-icon-info" style="float: left; margin: 0 7px 50px 0;"></span>This document has been sent to ' + email + '</p>');
				} else {
					HtmlAlert._alert('There has been an error', '<p><span class="ui-icon ui-icon-alert" style="float: left; margin: 0 7px 50px 0;"></span>This document has NOT been sent to ' + email + '</p>');
				}
			},
			type: 'GET',
			url: self.urls.email.format(documentId, email)
		});
	},

	_print: function(printerId, documentId) {
		var self = this;
		$.ajax({
			beforeSend: function() {/* Add throbber */ },
			complete: function() {/* Remove throbber */ },
			error: this.ajaxErrorHandler,
			global: false,
			success: function(data) {
				if (data.status == 'success') {
					HtmlAlert._alert('Print has been queue', '<p><span class="ui-icon ui-icon-info" style="float: left; margin: 0 7px 50px 0;"></span>This document has been sent to the printer</p>');
				} else {
					HtmlAlert._alert('There has been an error', '<p><span class="ui-icon ui-icon-alert" style="float: left; margin: 0 7px 50px 0;"></span>This document has NOT been sent to the printer</p>');
				}
			},
			type: 'GET',
			url: self.urls.print.format(printerId, documentId)
		});
	},

	email: function(documentId) {
		if (documentId) {
			$('#email-documentId').val(documentId);
		} else {
			$('#email-documentId').val($('#documentId').val());
		}

		$('#email-dialog').dialog('open');
	},

	getPage: function(documentId, pageNumber, callback) {
		var self = this;
		$.ajax({
			beforeSend: function() {/* Add throbber */ },
			complete: function() {/* Remove throbber */ },
			error: self.ajaxErrorHandler,
			global: false,
			success: function(data) {
				if (data.pageNumber) {
					var bg = new Image();
					bg.src = self.urls.downloadImage.format(documentId, pageNumber);
					if ($.isFunction(callback)) {
						callback({
							lines: new Array(),
							background: bg,
							pageNumber: data.pageNumber,
							sourceHeight: data.sourceHeight,
							sourceWidth: data.sourceWidth
						});
					}
				}
			},
			type: 'GET',
			url: self.urls.image.format(documentId, pageNumber)
		});
	},

	print: function(documentId) {
		if (documentId) {
			$('#print-documentId').val(documentId);
		} else {
			$('#print-documentId').val($('#documentId').val());
		}

		$('#printer-select').dialog('open');
	},

	submitPages: function(documentId, lines, callback) {
		var self = this;
		$.ajax({
			beforeSend: function() {/* Add throbber */ },
			complete: function() {/* Remove throbber */ },
			data: {lines: JSON.stringify(lines)},
			error: self.ajaxErrorHandler,
			global: false,
			success: function(data) {
				if ($.isFunction(callback)) {
					callback();
				}
			},
			type: 'POST',
			url: self.urls.sign.format(documentId)
		});
	},

	init: function(urls, ajaxErrorHandler) {
		var self = this;
		this.urls = urls;
		this.ajaxErrorHandler = ajaxErrorHandler;

		$('#printer-select').removeClass('hidden').dialog({
			autoOpen: false,
			buttons: {
				'Print': function() {
					$(this).dialog('close');
					self._print($('#printer').val(), $('#print-documentId').val());
				},
				'Cancel' :function() {
					$(this).dialog('close');
				}
			},
			closeOnEscape: false,
			draggable: false,
			modal: true,
			open: function(event, ui) {},
			resizable: false,
			width: 400
		});

		$('#email-dialog').dialog({
			autoOpen: false,
			buttons: {
				'Email': function() {
					$(this).dialog('close');
					self._email($('#email-documentId').val(), $('#address').val());
				},
				'Cancel' :function() {
					$(this).dialog('close');
				}
			},
			closeOnEscape: false,
			draggable: false,
			modal: true,
			open: function(event, ui) {},
			resizable: false,
			width: 400
		});
	}
};
