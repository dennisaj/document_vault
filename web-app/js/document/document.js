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

	_print: function(printerId, documentId, addNotes) {
		return $.ajax({
			data: { addNotes:addNotes },
			error: this.ajaxErrorHandler,
			global: false,
			success: function(data) {
				// TODO i18n
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

	getNotes: function(callback) {
		return $.when($.post(this.urls.listNotes.format(this.documentId))).fail(this.ajaxErrorHandler);
	},

	saveTextNote: function(note) {
		var self = this;
		var dfd = $.Deferred();

		this.getPage(note.pageNumber).done(function(page) {
			var scaledNote = self._scaleOutgoingNote(page, note);
			$.when($.post(self.urls.saveTextNote.format(self.documentId), { value:scaledNote.note, pageNumber:scaledNote.pageNumber, left:scaledNote.left, top:scaledNote.top }))
				.done(function(data) { dfd.resolve(data); })
				.fail(self.ajaxErrorHandler);
		});

		return dfd;
	},

	saveNotes: function(notes) {
		return $.when($.post(this.urls.saveNotes.format(this.documentId), { notes:JSON.stringify(notes) })).fail(this.ajaxErrorHandler);
	},

	/**
	 * Bypass the page cache and load directly from the server.
	 */
	_getPage: function(pageNumber, callback) {
		var self = this;
		var dfd = $.Deferred();

		$.ajax({
			error: this.ajaxErrorHandler,
			global: false,
			success: function(data) {
				if (data.pageNumber) {
					var bg = new Image();
					bg.src = self.urls.downloadImage.format(self.documentId, pageNumber);

					// If the page already exists, preserve the unsaved highlights of new parties.
					// This prevents highlights from being lost when there is an error saving a new party.
					var unsavedHighlights = {}
					if (self.pages[data.pageNumber]) {
						$.each(self.pages[data.pageNumber].unsavedHighlights, function(key, value) { 
							if (key.match(/^[A-F\d]{8}(?:-[A-F\d]{4}){3}-[A-F\d]{12}$/i)) {
								unsavedHighlights[key] = value;
							}
						});
					}

					self.pages[data.pageNumber] = {
						background: bg,
						lines: new Array(),
						pageNumber: data.pageNumber,
						savedHighlights: data.highlights,
						scale: 1,
						scrollCanX: 0,
						scrollCanY: 0,
						sourceHeight: data.sourceHeight,
						sourceWidth: data.sourceWidth,
						unsavedHighlights: unsavedHighlights
					};

					if (self.pages[data.pageNumber].background.complete) {
						self.pages[data.pageNumber].savedHighlights = self._scaleHighlights(self.pages[data.pageNumber]);
					} else {
						bindEvent(self.pages[data.pageNumber].background, 'load', function() {
							self.pages[data.pageNumber].savedHighlights = self._scaleHighlights(self.pages[data.pageNumber]);
						});
					}
					dfd.resolve(self.pages[data.pageNumber]);
				} else {
					dfd.resolve();
				}
			},
			type: 'POST',
			url: this.urls.image.format(this.documentId, pageNumber)
		});

		return dfd;
	},

	/**
	 * Check the page cache and only load directly from the server if the page is not found.
	 */
	getPage: function(pageNumber, callback) {
		var self = this;

		if (pageNumber > this.pageCount) {
			pageNumber = this.pageCount;
		} else if (pageNumber < this.FIRST_PAGE) {
			pageNumber = this.FIRST_PAGE;
		}

		if (this.pages[pageNumber]) {
			return $.Deferred().resolve(this.pages[pageNumber]);
		} else {
			return this._getPage(pageNumber, callback);
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
		var self = this;
		documentId = documentId || this.documentId;

		// TODO: Add loading message
		return $.when($.post(this.urls.printWindow.format(documentId))).then(function(data) {
			$(data).dialog({
				buttons: {
					'Print': function() {
						var printer = $('#printer').val();
						var documentId = $('#print-documentId').val();
						var addNotes = $('#addNotes').prop('checked');
						$(this).dialog('close');
						self._print(printer, documentId, addNotes);
					},
					'Cancel' :function() {
						$(this).dialog('close');
					}
				},
				close: function(event, ui) {
					$(this).remove();
				},
				modal: true,
				open: function(event, ui) {
					var dialog = this;
					$('.ui-widget-overlay').click(function() {
						$(dialog).dialog('close');
					});
				},
				resizable: false,
				width: 'auto'
			});
		}).fail(this.ajaxErrorHandler);
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
					self._getPage(pageNumber).done(function(page) {
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
					height: round(highlight.height * scaleY),
					left: round(highlight.left * scaleX),
					width: round(highlight.width * scaleX),
					top: round(highlight.top * scaleY)
				};
			});
		});

		return scaledHighlights;
	},

	_scaleOutgoingNote: function(page, note) {
		var scaleX = page.sourceWidth / page.background.width;
		var scaleY = page.sourceHeight / page.background.height;

		var scaledNote = $.extend({}, note);
		scaledNote.left = round(scaledNote.left * scaleX);
		scaledNote.top = round(scaledNote.top * scaleY);

		return scaledNote;
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
	}
};
