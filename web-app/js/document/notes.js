var Notes = {
	baseFontSize: 24,
	DEBUG: false,
	notes: [],

	doEnd: function(event) {
		this.resetNotes();
		if (!InputHandler.isMoving) {
			var self = this;
			var canvas = event.currentTarget;
			var page = Sign.currentPage();
			var previousPoint = $.extend({}, InputHandler.previousPoint);
			var scaledPoint = Draw.scalePoint(page, previousPoint);
			var font = (this.baseFontSize * page.scale) + 'px';
			var width = (canvas.width - scaledPoint.x);

			var $container = $('<div />').prop('id', 'inline-note-container').css('font-size', font);
			$container.css({left:previousPoint.x, top:previousPoint.y}).appendTo($('body')).width(width * page.scale);
			var $submit = $('<input />').attr('value', 'Submit').attr('type', 'button').button();
			var $textarea = $('<textarea />').addClass('inline-note').appendTo($container);
			$textarea.TextAreaExpander();
			$textarea.after($submit);
			$textarea.focus();
			$submit.bind('click', function(event) {
				var note = $textarea.val().trim();
				self.multiFillText(canvas, note, scaledPoint.x, scaledPoint.y, width);
				self.saveNote({note:note, page:page.pageNumber, left:scaledPoint.x, top:scaledPoint.y});
				self.resetNotes();
			});
		}

		$('#main').css('cursor', 'crosshair');
	},

	doGestureChange: function(event) {
		this.resetNotes();
		Sign.doGestureChange(event);
	},

	doMove: function(event) {
		this.resetNotes();
		Sign.doMove(event);
	},

	doStart: function(event) {
		// Set initialZoom so that pinch to zoom works when notes.js is active.
		Sign.initialZoom = $('#slider').slider('value');
	},

	drawNotes: function(canvas, page) {
		if (InputHandler.handlingInput !== this) {
			return;
		}

		var self = this;
		$.each(this.notes[page.pageNumber] || [], function(index, note) {
			var width = canvas.width - note.left;
			self.multiFillText(canvas, note.note, note.left, note.top, width);
		});
	},

	getNotes: function() {
		var self = this;

		return Document.getNotes().then(function(data) {
			self.notes = [];

			$.each(data, function(index, note) {
				self.notes[note.page] = self.notes[note.page] || {};
				self.notes[note.page][index] = {
					left: note.left,
					top: note.top,
					note: note.note
				}
			});
		});
	},

	saveNote: function(note) {
		note.note = note.note.trim();
		if (!note.note) {
			return;
		}

		var self = this;

		Document.saveTextNote(note).always(function() {
			self.getNotes().always(function() {
				Draw.draw(Sign.can, Sign.currentPage());
			});
		});
	},

	/**
	 * Borrowed from http://stackoverflow.com/questions/4478742/html5-canvas-can-i-somehow-use-linefeeds-in-filltext/7029882#7029882
	 */
	multiFillText: function(canvas, text, x, y, fitWidth) {
		var currentLine = 0, maxHeight = 0, maxWidth = 0;
		var ctx = canvas.getContext('2d');
		var lineHeight = this.baseFontSize;

		ctx.font = this.baseFontSize + 'px sans-serif';
		ctx.fillStyle = 'black';
		ctx.textAlign = 'start';
		ctx.textBaseline = 'top';

		text = text.replace(/(\r\n|\n\r|\r|\n)/g, '\n');
		var sections = text.split('\n');

		var printNextLine = function(str, currentLine) {
			ctx.fillText(str, x, y + (lineHeight * currentLine));

			var wordWidth = ctx.measureText(str).width;
			if (wordWidth > maxWidth) {
				maxWidth = wordWidth;
			}
		};

		for (var i = 0; i < sections.length; i++) {
			var words = sections[i].split(' ');
			var index = 1;

			while (words.length > 0 && index <= words.length) {
				var str = words.slice(0, index).join(' ');
				var wordWidth = ctx.measureText(str).width;

				if (wordWidth > fitWidth) {
					if (index === 1) {
						// Falls to this case if the first word in words[] is bigger than fitWidth
						// so we print this word on its own line; index = 2 because slice is
						str = words.slice(0, 1).join(' ');
						words = words.splice(1);
					} else {
						str = words.slice(0, index - 1).join(' ');
						words = words.splice(index - 1);
					}

					printNextLine(str, currentLine);
					currentLine++;

					index = 1;
				} else {
					index++;
				}
			}

			// The left over words on the last line
			if (index > 0) {
				printNextLine(words.join(' '), currentLine);
				currentLine++;
			}
		}

		maxHeight = lineHeight * currentLine;

		if (this.DEBUG) {
			ctx.strokeRect(x, y, maxWidth, maxHeight);
		}

		return {
			height: maxHeight,
			width: maxWidth
		};
	},

	resetNotes: function() {
		$('#inline-note-container').remove();
	},

	init: function() {
		var self = this;

		this.getNotes();

		Draw.addDrawEvent('notes', function(canvas, page) {
			self.drawNotes(canvas, page);
		});

		$('#notes').button({
			icons: { primary: 'ui-icon-note' }
		}).bind('marked', function(event) {
			var $this = $(this);

			if ($this.is('.ui-state-highlight')) {
				InputHandler.handlingInput = self;
			} else {
				self.resetNotes();
				InputHandler.handlingInput = Sign;
			}

			Draw.draw(Sign.can, Sign.currentPage());
		});

		$('#slider').bind('slidechange', function(event) {
			self.resetNotes();
		});
	}
};
