var Document = {
	ajaxErrorHandler: function() {},
	documentId: null,
	FIRST_PAGE: 1,
	pageCount: 0,
	pages: null,
	urls: {},

	_areAllPagesLoaded: function() {
		for (var pageNumber = this.FIRST_PAGE; pageNumber <= this.pageCount; pageNumber++) {
			var page = this.pages[pageNumber]
			if (!page || !page.background.complete) {
				return false;
			}
		}

		return true;
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
			url: this.urls.print.format(printerId, documentId)
		});
	},

	getDocumentId: function() {
		return $('#documentId').val();
	},

	getPage: function(pageNumber, callback) {
		var self = this;

		if (pageNumber > this.pageCount) {
			pageNumber = this.pageCount;
		} else if (pageNumber < this.FIRST_PAGE) {
			pageNumber = this.FIRST_PAGE;
		}

		if (this.pages[pageNumber]) {
			if ($.isFunction(callback)) {
				callback(this.pages[pageNumber]);
			}
		} else {
			$.ajax({
				beforeSend: function() {/* Add throbber */ },
				complete: function() {/* Remove throbber */ },
				error: this.ajaxErrorHandler,
				global: false,
				success: function(data) {
					if (data.pageNumber) {
						var bg = new Image();
						bg.src = self.urls.downloadImage.format(self.documentId, pageNumber);

						self.pages[data.pageNumber] = {
							highlights: data.highlights,
							lines: new Array(),
							background: bg,
							pageNumber: data.pageNumber,
							sourceHeight: data.sourceHeight,
							sourceWidth: data.sourceWidth
						};

						if ($.isFunction(callback)) {
							callback(self.pages[data.pageNumber]);
						}
					}
				},
				type: 'POST',
				url: this.urls.image.format(this.documentId, pageNumber)
			});
		}
	},

	/**
	 * Load all pages then perform the callback once the last page is loaded.
	 * 
	 * If all pages are already loaded, perform the callback immediately.
	 */
	loadAllPages: function(callback) {
		var self = this;

		if (self._areAllPagesLoaded()) {
			callback();
		} else {
			for (var pageNumber = this.FIRST_PAGE; pageNumber <= this.pageCount; pageNumber++) {
				// Skip this page if it has already been loaded
				if (!this.pages[pageNumber]) {
					self.getPage(pageNumber, function(page) {
						self.pages[page.pageNumber] = page;

						if ($.isFunction(callback)) {
							if (page.background.complete) {
								if (self._areAllPagesLoaded()) {
									callback();
								}
							} else {
								page.background.onload = function() {
									if (self._areAllPagesLoaded()) {
										callback();
									}
								};
							}
						}
					});
				}
			}
		}
	},

	print: function(documentId) {
		if (documentId) {
			$('#print-documentId').val(documentId);
		} else {
			$('#print-documentId').val(this.documentId);
		}

		$('#printer-select').dialog('open');
	},

	submitParties: function(parties, callback) {
		var self = this;
		$.ajax({
			beforeSend: function() {/* Add throbber */ },
			complete: function() {/* Remove throbber */ },
			data: {parties: JSON.stringify(parties)},
			error: this.ajaxErrorHandler,
			global: false,
			success: function(data) {
				if ($.isFunction(callback)) {
					callback(data);
				}
			},
			type: 'POST',
			url: this.urls.submitParties.format(this.documentId)
		});
	},

	submitLines: function(lines, callback) {
		var self = this;
		$.ajax({
			beforeSend: function() {/* Add throbber */ },
			complete: function() {/* Remove throbber */ },
			data: {lines: JSON.stringify(lines)},
			error: this.ajaxErrorHandler,
			global: false,
			success: function(data) {
				if ($.isFunction(callback)) {
					callback();
				}
			},
			type: 'POST',
			url: this.urls.sign.format(this.documentId)
		});
	},

	init: function(urls, ajaxErrorHandler) {
		var self = this;

		this.documentId = this.getDocumentId();
		this.pageCount = parseInt($('#pageCount').val() || this.FIRST_PAGE);
		this.pages = new Array(this.pageCount + this.FIRST_PAGE);
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
	}
};
