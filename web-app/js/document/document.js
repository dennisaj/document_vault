var Document = {
	documentId: null,
	FIRST_PAGE: 1,
	pageCount: 0,
	pages: null,
	urls: {},

	ajaxErrorHandler: function(jqXHR, textStatus, errorThrown) {
		// TODO: Unify error handling
		$('#signature-message, #party-message').dialog('close');

		HtmlAlert._alert('An error has occurred', '<p><span class="ui-icon ui-icon-alert" style="float: left; margin: 0 7px 50px 0;"></span>There was an error communicating with the server. Please try again.</p>');
	},

	_areAllCachedPagesLoaded: function() {
		for (var pageNumber = this.FIRST_PAGE; pageNumber <= this.pageCount; pageNumber++) {
			var page = this.pages[pageNumber];
			if (page && !page.background.complete) {
				return false;
			}
		}

		return true;
	},

	_areAllPagesLoaded: function() {
		for (var pageNumber = this.FIRST_PAGE; pageNumber <= this.pageCount; pageNumber++) {
			var page = this.pages[pageNumber];
			if (!page || !page.background.complete) {
				return false;
			}
		}

		return true;
	},

	_areAnyPagesLoaded: function() {
		for (var pageNumber = this.FIRST_PAGE; pageNumber <= this.pageCount; pageNumber++) {
			var page = this.pages[pageNumber];
			if (page && page.background.complete) {
				return true;
			}
		}

		return false;
	},

	_print: function(printerId, documentId) {
		var self = this;
		$.ajax({
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

	/**
	 *  Bypass the page cache and load directly from the server.
	 */
	_getPage: function(pageNumber, callback) {
		var self = this;

		$.ajax({
			error: this.ajaxErrorHandler,
			global: false,
			success: function(data) {
				if (data.pageNumber) {
					var bg = new Image();
					bg.src = self.urls.downloadImage.format(self.documentId, pageNumber);

					self.pages[data.pageNumber] = {
						savedHighlights: data.highlights,
						unsavedHighlights: {},
						lines: new Array(),
						background: bg,
						pageNumber: data.pageNumber,
						sourceHeight: data.sourceHeight,
						sourceWidth: data.sourceWidth
					};

					if (self.pages[data.pageNumber].background.complete) {
						self.pages[data.pageNumber].savedHighlights = self._scaleHighlights(self.pages[data.pageNumber]);
					} else {
						self.pages[data.pageNumber].background.addEventListener('load', function() {
							self.pages[data.pageNumber].savedHighlights = self._scaleHighlights(self.pages[data.pageNumber]);
						}, false);
					}

					if ($.isFunction(callback)) {
						callback(self.pages[data.pageNumber]);
					}
				}
			},
			type: 'POST',
			url: this.urls.image.format(this.documentId, pageNumber)
		});
	},

	/**
	 *  Check the page cache and only load directly from the server if the page is not found.
	 */
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
			this._getPage(pageNumber, callback);
		}
	},

	/**
	 * Load all uncached pages then perform the callback once the last page is loaded.
	 * 
	 * If all pages are already loaded, perform the callback immediately.
	 */
	loadAllPages: function(callback) {
		var self = this;

		if (self._areAllPagesLoaded()) {
			if ($.isFunction(callback)) {
				callback();
			}
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

	refreshPageCache: function(callback) {
		var self = this;

		if (!this._areAnyPagesLoaded()) {
			if ($.isFunction(callback)) {
				callback();
			}
		} else {
			for (var pageNumber = this.FIRST_PAGE; pageNumber <= this.pageCount; pageNumber++) {
				// Skip this page if it has not already been loaded
				if (this.pages[pageNumber]) {
					self._getPage(pageNumber, function(page) {
						self.pages[page.pageNumber] = page;

						if ($.isFunction(callback)) {
							if (page.background.complete) {
								if (self._areAllCachedPagesLoaded()) {
									callback();
								}
							} else {
								page.background.onload = function() {
									if (self._areAllCachedPagesLoaded()) {
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

	/**
	 * Returns a deferred object to which callbacks can be attached.
	 */
	removeParty: function(partyId) {
		return $.when($.post(this.urls.removeParty.format(this.documentId, partyId))).fail(this.ajaxErrorHandler);
	},

	/**
	 * Returns a deferred object to which callbacks can be attached.
	 */
	resendCode: function(partyId) {
		return $.when($.post(this.urls.resendCode.format(this.documentId, partyId))).fail(this.ajaxErrorHandler); 
	},

	_scaleHighlights: function(page) {
		var scaleX = page.background.width / page.sourceWidth;
		var scaleY = page.background.height / page.sourceHeight;

		var scaledHighlights = {};

		$.each(page.savedHighlights, function(partyId, highlights) {
			scaledHighlights[partyId] = [];
			$.each(highlights, function(index, highlight) {
				scaledHighlights[partyId][index] = {
					upperLeftCorner: {
						x:Sign._round(highlight.upperLeftCorner.x * scaleX),
						y:Sign._round(highlight.upperLeftCorner.y * scaleY)
					},
					lowerRightCorner: {
						x:Sign._round(highlight.lowerRightCorner.x * scaleX),
						y:Sign._round(highlight.lowerRightCorner.y * scaleY)
					}
				};
			});
		});

		return scaledHighlights;
	},

	/**
	 * Returns a deferred object to which callbacks can be attached.
	 */
	submitParties: function(parties) {
		return $.when($.post(this.urls.submitParties.format(this.documentId), {parties: JSON.stringify(parties)})).fail(this.ajaxErrorHandler);
	},

	/**
	 * Returns a deferred object to which callbacks can be attached.
	 */
	submitLines: function(lines, callback) {
		return $.when($.post(this.urls.sign.format(this.documentId), {lines: JSON.stringify(lines)})).fail(this.ajaxErrorHandler);
	},

	init: function(urls) {
		var self = this;

		this.documentId = this.getDocumentId();
		this.pageCount = parseInt($('#pageCount').val() || this.FIRST_PAGE);
		this.pages = new Array(this.pageCount + this.FIRST_PAGE);
		this.urls = urls;

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
