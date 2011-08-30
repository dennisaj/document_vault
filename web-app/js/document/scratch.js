var Scratch = {
	currentNote: null,
	defaultWidth: 768,
	defaultHeight: 576,
	isMouseDown: false,
	isMoving: false,
	notes: {},
	previousPoint: {},
	scratch: null,
	trackingTouchId: null,

	addNote: function(noteId, src) {
		var bg = new Image();
		if (src) {
			bg.src = src;
		}

		this.notes[noteId] = {
			background: bg,
			lines: new Array(),
			pageNumber: noteId,
			savedHighlights: [],
			scale: 1,
			scrollCanX: 0,
			scrollCanY: 0,
			sourceHeight: this.defaultHeight,
			sourceWidth: this.defaultWidth,
			unsavedHighlights: []
		};

		this.notes[noteId].background.height = this.defaultHeight;
		this.notes[noteId].background.width = this.defaultWidth;

		return this.notes[noteId];
	},

	_addEmptyScratchBox: function(note) {
		var $scratchBox = $('<div />').attr('id', 'scratch-box' + note.pageNumber).addClass('scratch-box');
		$scratchBox.append('<img />');
		$scratchBox.data('scratch-id', note.pageNumber);
		$('#add-scratch-box').before($scratchBox);
	},

	_addScratchBoxImage: function(note) {
		var $scratchBox = $('#scratch-box' + note.pageNumber);
		this._setPreview(note, $scratchBox);
	},

	_closeScratch: function() {
		var note = this.notes[this.currentNote];
		if (note) {
			$('#scratch-canvas-container').hide();
			this._setPreview(note, $('#scratch-box' + note.pageNumber));
			$('.scratch-box.toggle').removeClass('toggle');
		}
	},

	doEnd: function(event, canvas, note) {
		if (this._isSaved(note)) {
			return;
		}

		if (!this.isMoving) {
		} else {
			Draw.addBreak(note);
		}

		this.isMoving = false;
	},

	doMove: function(event, canvas, note) {
		if (this._isSaved(note)) {
			return;
		}

		this.isMoving = true;
		var point = Draw.convertEventToPoint(event);

		var line = {
			start: Draw.scalePoint(note, this.previousPoint),
			end: Draw.scalePoint(note, point),
		};
		Draw.addLine(note, line);
		Draw.drawLine(canvas, line);

		this.previousPoint = point;
	},

	getNotes: function() {
		var self = this;

		Document.getNotes().then(function(data) {
			$('.scratch-box').remove();
			self.notes = {};

			$.each(data, function(index, noteInfo) {
				var note = self.addNote(index, noteInfo.url);
				self._addEmptyScratchBox(note);
				if (note.background.complete) {
					self._addScratchBoxImage(note);
				} else {
					bindEvent(note.background, 'load', function() {
						self._addScratchBoxImage(note);
					});
				}
			});
		});
	},

	_isSaved: function(note) {
		return note.pageNumber > 0;
	},

	saveNotes: function() {
		var self = this;
		var notes = {};

		$.each(this.notes, function(index, note) {
			if (!self._isSaved(note) && note.lines.length > 0) {
				notes[index] = Draw.scaleLines(note);
			}
		});

		if (Object.keys(notes).length > 0) {
			Document.saveNotes(notes).always(function() {
				self.getNotes();
			});
		}
	},

	_saveScratch: function() {
		this._closeScratch();
		this.saveNotes();
	},

	_setPreview: function(note, $target) {
		var cleanCanvas = document.createElement('canvas');
		cleanCanvas.width = note.background.width;
		cleanCanvas.height = note.background.height;

		Draw.draw(cleanCanvas, note);
		var image = new Image();
		image.src = cleanCanvas.toDataURL('image/png');
		$target.html($(image));
		cleanCanvas = null;
	},

	init: function() {
		var self = this;
		var $scratch = $('#scratch');
		var eventType = $.support.touch ? 'touchend' : 'click';
		this.scratch = $scratch[0];

		this.getNotes();

		$('.scratch-box').live(eventType, function(event) {
			event.originalEvent.stopPropagation();

			var $this = $(this);
			self.currentNote = $this.data('scratch-id');
			var note = self.notes[self.currentNote];

			$('.scratch-box.toggle').removeClass('toggle');
			$(this).addClass('toggle');

			$('#scratch-edit-buttons').toggle(!self._isSaved(note));

			self.scratch.width = note.background.width;
			self.scratch.height = note.background.height;
			$('#scratch-canvas-container').show();
			Draw.draw(self.scratch, note);
		});

		$('#add-scratch-box').bind(eventType, function(event) {
			var note = self.addNote(-($('.scratch-box').length + 1));
			self._addEmptyScratchBox(note);
		});

		$('#close-scratch').button({
			icons: { primary: 'ui-icon-circle-close' }
		}).bind(eventType, function(event) {
			self.notes[self.currentNote].lines.splice(0, self.notes[self.currentNote].lines.length);
			self._closeScratch()
		});

		$('#clear-scratch').button({
			icons: { primary: 'ui-icon-refresh' }
		}).bind(eventType, function(event) {
			self.notes[self.currentNote].lines.splice(0, self.notes[self.currentNote].lines.length);
			Draw.draw(self.scratch, self.notes[self.currentNote]);
		});

		$('#undo-scratch').button({
			icons: { primary: 'ui-icon-arrowreturnthick-1-w' }
		}).bind(eventType, function(event) {
			Draw.undo(self.scratch, self.notes[self.currentNote]);
		});

		$('#back-to-sign').button({
			icons: { primary: 'ui-icon-circle-arrow-w' }
		}).bind(eventType, function(event) {
			self._saveScratch();
			$('#canvas-container').removeClass('flip');
			$('#button-container').removeClass('flip');
		});

		$('#save-scratch').button({
			icons: { primary: 'ui-icon-disk' }
		}).bind(eventType, function(event) {
			self._saveScratch();
		});

		if ($.support.touch) {
			$scratch.bind('touchstart', function(event) {
				if (self.trackingTouchId == null) {
					var e = event.originalEvent;
					var touch = e.targetTouches[0];

					self.trackingTouchId = touch.identifier;
					self.previousPoint = Draw.convertEventToPoint(touch);
				}
			}).bind('touchmove', function(event) {
				var e = event.originalEvent;
				var currentTouch = null;

				for (var i = 0; i < e.touches.length; i++) {
					if (self.trackingTouchId == e.touches[i].identifier) {
						currentTouch = e.touches[i];
					}
				}

				if (currentTouch) {
					self.doMove(currentTouch, self.scratch, self.notes[self.currentNote]);
				}
			}).bind('touchend touchcancel', function(event) {
				var e = event.originalEvent;
				var currentTouch = null;

				for (var i = 0; i < e.changedTouches.length; i++) {
					if (self.trackingTouchId == e.changedTouches[i].identifier) {
						currentTouch = e.changedTouches[i];
					}
				}

				if (currentTouch) {
					self.trackingTouchId = null;
					self.doEnd(currentTouch, self.scratch, self.notes[self.currentNote]);
				}
			});
		} else {
			$scratch.bind('mousedown', function(e) {
				if (e.which == 1) {
					self.isMouseDown = true;
					self.previousPoint = Draw.convertEventToPoint(e);
				}
			}).bind('mousemove', function(e) {
				if (self.isMouseDown) {
					self.doMove(e, self.scratch, self.notes[self.currentNote]);
				}
			}).bind('mouseup mouseleave', function(e) {
				if (self.isMouseDown) {
					self.doEnd(e, self.scratch, self.notes[self.currentNote]);
					self.isMouseDown = false;
				}
			});
		}
	}
};
