var Document = {
	ajaxErrorHandler: function() {},
	urls: {},

	_email: function(documentId, email) {
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
			url: Document.urls['email'].format(documentId, email)
		});
	},

	_print: function(printerId, documentId) {
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
			url: Document.urls['print'].format(printerId, documentId)
		});
	},

	email: function() {
		$('#email-dialog').dialog('open');
	},

	finishDocument: function(documentId, callback) {
		$.ajax({
			beforeSend: function() {/* Add throbber */ },
			complete: function() {/* Remove throbber */ },
			error: Document.ajaxErrorHandler,
			global: false,
			success: function(data) {
				callback();
			},
			type: 'GET',
			url: Document.urls['finish'].format(documentId)
		});
	},

	getPage: function(documentId, pageNumber, callback) {
		$.ajax({
			beforeSend: function() {/* Add throbber */ },
			complete: function() {/* Remove throbber */ },
			error: Document.ajaxErrorHandler,
			global: false,
			success: function(data) {
				if (data.imageData) {
					var bg = new Image();
					bg.src = data.imageData;
					if (callback) {
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
			url: Document.urls['image'].format(documentId, pageNumber)
		});
	},

	print: function() {
		$('#printer-select').dialog('open');
	},

	submitPage: function(documentId, pageNumber, imageData, callback) {
		$.ajax({
			beforeSend: function() {/* Add throbber */ },
			complete: function() {/* Remove throbber */ },
			data: {imageData: imageData},
			error: Document.ajaxErrorHandler,
			global: false,
			success: function(data) {
				callback(documentId, pageNumber + 1);
			},
			type: 'POST',
			url: Document.urls['sign'].format(documentId, pageNumber)
		});
	},

	init: function(urls, ajaxErrorHandler) {
		Document.urls = urls;
		Document.ajaxErrorHandler = ajaxErrorHandler;

		$('#printer-select').dialog({
			autoOpen: false,
			buttons: {
				'Print': function() {
					$(this).dialog('close');
					Document._print($('#printer').val(), $('#documentId').val());
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
					Document._email($('#documentId').val(), $('#address').val());
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
