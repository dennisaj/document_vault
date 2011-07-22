var Party = {
	signPermission: 'sign',
	urls: {},

	addParty: function(data) {
		var $data = $(data);

		$data = this.setupParty($data);

		$('#party-container').append($data);

		// Always select the new row.
		$('[name="selectedParty"]', '#party-container').prop('checked', true).change();

		//this._rowChangeCallback({target:$('#' + $data.attr('id'))});
	},

	_afterRemoveCallback: function($party) {
		this.refreshHighlightButton();
		$party.remove();

		if (!this.getSelectedPartyRow().length) {
			$('[name="selectedParty"]:first').prop('checked', true).change();
		}

		Sign.draw(Sign.can, Sign.currentPage());
	},

	canChangeColor: function($partyRow) {
		return this._canSign($partyRow) && !$partyRow.children('.permission').prop('disabled')
	},

	_canSign: function($partyRow) {
		return ($partyRow.children('.permission').val() || '').toLowerCase() === this.signPermission;
	},

	canSign: function(party) {
		return this._canSign($('#' + party));
	},

	getCurrentColor: function() {
		return this._getPartyColor(this.getSelectedPartyRow());
	},

	_getUnsavedHighlights: function() {
		var highlights = {};

		$.each(Document.pages, function(j, page) {
			if (page && page.unsavedHighlights) {
				var scaledPage = Sign._scaleCorners(page);
				for (var party in page.unsavedHighlights) {
					highlights[party] = highlights[party] || new Array();
					highlights[party][page.pageNumber] = scaledPage[party];
				}
			}
		});

		return highlights;
	},

	_getParties: function() {
		var highlights = this._getUnsavedHighlights();
		var parties = new Array();

		$.each($('.party'), function(i, party) {
			var code = $(party).attr('id');

			if (code) {
				var partyId = $('#id-' + code).val()

				// If partyId is set, we are updating. If not, we are adding.
				if (partyId) {
					parties.push({
						id: partyId,
						color: $('#color-' + code).val(),
						highlights: highlights[code]
					});
				} else {
					parties.push({
						code: code,
						fullName: $('#fullName-' + code).val(),
						email: $('#email-' + code).val(),
						expiration: $('#expiration-' + code).val(),
						color: $('#color-' + code).val(),
						permission: $('#permission-' + code).val(),
						highlights: highlights[code]
					});
				}
			}
		});

		return parties;
	},

	_getPartyColor: function($partyRow) {
		return $partyRow.children('.color').val();
	},

	getPartyColor: function(party) {
		return this._getPartyColor($('#' + party));
	},

	getSelectedPartyRow: function() {
		return $('[name="selectedParty"]:checked:visible', '#party-container').parent();
	},

	_getTargetRow: function($target) {
		return $target.is('.party') ? $target : $target.parent();
	},

	initParties: function() {
		var self = this;
		$.each($('.party'), function(i, party) {
			var $party = $(party);
			self.setupParty($(party)).find('[name="selectedParty"]').prop('checked', true);
		});
	},

	isRequestingSignatures: function() {
		return $('#get-signed').is('.ui-state-highlight') || $('#show-highlights').is('.ui-state-highlight');
	},

	isHighlighting: function() {
		var $highlight = $('#highlight');
		return $highlight.is('.ui-state-highlight') && !$highlight.prop('disabled');
	},

	refreshHighlightButton: function() {
		var $highlight = $('#highlight');
		var $currentRow = this.getSelectedPartyRow();

		if ($currentRow.length) {
			$highlight.button(this._canSign($currentRow) ? 'enable' : 'disable');
		}

		$('#sample', $highlight).css('background-color', this._getPartyColor($currentRow));
	},

	_rowChangeCallback: function(event) {
		this.refreshHighlightButton();

		var $party = this._getTargetRow($(event.target));

		$party.children('.color').prop('disabled', !this.canChangeColor($party));
		Sign.draw(Sign.can, Sign.currentPage());
	},

	setupParty: function($party) {
		var self = this;
		$('button.remove', $party).button({
			icons: { primary: 'ui-icon-closethick' },
			text: false
		}).click(function(event) {
			$('#confirm-remove').data().dialog['$party'] = $(this).parent();
			$('#confirm-remove').dialog('open');
		});

		$('button.resend', $party).button({
			icons: { secondary: 'ui-icon-mail-closed' }
		}).click(function(event) {
			var $this = $(this);
			var $party = $this.parent();
			var code = $party.attr('id');
			var partyId = $('#id-' + code).val()

			// If partyId is set, resend. If not, continue.
			if (partyId) {
				$this.button('disable');
				$('#party-message').dialog('open');

				Document.resendCode(partyId).always(function() {
					$('#party-message').dialog('close');
					$this.button('enable');
				});
			}
		});

		$('.expiration', $party).datepicker({
			minDate: 0,
			showButtonPanel: true
		});

		$('.color', $party).bind('change keyup', function(event) {
			self.refreshHighlightButton();
			Sign.draw(Sign.can, Sign.currentPage());
		});

		$party.children('.color').prop('disabled', !this.canChangeColor($party));

		return $party;
	},

	startHighlighting: function() {
		return $('#highlight').addClass('ui-state-highlight');
	},

	stopHighlighting: function() {
		return $('#highlight').removeClass('ui-state-highlight');
	},

	// document.js functions
	submitParties: function() {
		var self = this;

		var parties = self._getParties();

		Document.submitParties(parties).then(function(data) {
			$('#party-message').dialog('close');
			$('#party-container').html(data);
			Document.refreshPageCache();
			self.initParties();
		});
	},
	// !document.js functions

	init: function(urls) {
		this.urls = urls;
		var self = this;

		$('#request-signature').slideUp();

		$('#add-party').button({
			icons: { primary: 'ui-icon-contact' }
		});

		$('#submit-parties').button({
			icons: { secondary: 'ui-icon-arrowthick-1-e' }
		}).click(function(event) {
			$('#party-message').dialog('open');
			self.submitParties();
		});

		$('[name="selectedParty"], [name="permissionSelect"]').live('change', function(event) {
			self._rowChangeCallback(event);
		});

		// When a row is clicked, mark the row as selected
		$('.party').live('click', function(event) {
			var $target = $(event.target); 
			if ($target.is('.party') && !$target.is('label')) {
				$(this).find('[name="selectedParty"]').prop('checked', true);
				self._rowChangeCallback(event);
			}
		});

		$('#highlight').button({
			icons: { primary: 'ui-icon-flag' }
		}).button('disable').click(function(event) {
			// Refresh the canvas when the highlight button is disabled/enabled.
			Sign.draw(Sign.can, Sign.currentPage());
		});

		$('#get-signed').button({
			icons: { primary: 'ui-icon-person' }
		}).click(function() {
			var $this = $(this);
			var wasOn = $this.is('.ui-state-highlight');

			if (wasOn) {
				$('#request-signature').slideUp();
				$this.removeClass('ui-state-highlight');
				$('#pen, #save').button('enable');
				self.stopHighlighting().button('disable');
			} else {
				$('#request-signature').slideDown();
				$this.addClass('ui-state-highlight');
				$('#pen, #save').removeClass('ui-state-highlight').button('disable');
				$('#highlight').button('enable');
				self.refreshHighlightButton();
			}

			Sign.draw(Sign.can, Sign.currentPage());
		});

		$('#show-highlights').button({
			icons: { primary: 'ui-icon-lightbulb' }
		}).click(function() {
			var $this = $(this);

			if ($this.is('.ui-state-highlight')) {
				$('#request-signature').slideUp();
				$this.removeClass('ui-state-highlight');
			} else {
				$('#request-signature').slideDown();
				$this.addClass('ui-state-highlight');
			}

			Sign.draw(Sign.can, Sign.currentPage());
		});

		$('#party-message').dialog({
			autoOpen: false,
			buttons: {},
			closeOnEscape: false,
			draggable: false,
			modal: true,
			open: function(event, ui) {
				$(".ui-dialog-titlebar-close", $(this).parent()).hide();
				$(this).dialog('option', 'buttons', {});
			},
			resizable: false
		});

		$('#confirm-remove').dialog({
			autoOpen: false,
			buttons: {// TODO i18n
				'Remove': function(event) {
					$(this).dialog('close');
					var $party = $('#confirm-remove').data().dialog.$party;
					var code = $party.attr('id');
					var partyId = $('#id-' + code).val()

					// If partyId is set, this is an existing Party. If not, just remove the row.
					if (partyId) {
						$('#party-message').dialog('open');

						Document.removeParty(partyId).then(function() {
							$('#party-message').dialog('close');
							self._afterRemoveCallback($party);
						});
					} else {
						self._afterRemoveCallback($party);
					}
				},// TODO i18n
				'Cancel' :function() {
					$(this).dialog('close');
				}
			},
			draggable: false,
			modal: true,
			resizable: false
		});

		this.initParties();
	}
};
