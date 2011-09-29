var SignBox = {
	boxes: [],
	boxScale: 1,
	heightScale: 0.5,
	isMouseDown: false,
	isMoving: false,
	$main: null,
	page: {
		lines: [],
		background: null
	},
	partyColor: '#ff0',
	partyName: 'sign-box',
	previousPoint: { x:0, y:0 },
	// Add a little offset to compensate for touch screen inaccuracy.
	touchOffset: 30,
	trackingTouchId: null,
	widthScale: 0.4,

	addBox: function(box) {
		boxes.push(box);
	},

	doEnd: function(element, point) {
		this.trackingTouchId = null;
		this.isMouseDown = false;
		var $this = $(element);

		point = { x: point.x - $this.offset().left, y: point.y - $this.offset().top };

		// If the canvas was tapped, draw a point.
		if (!this.isMoving && point.x == this.previousPoint.x && point.y == this.previousPoint.y) {
			point.y += 5;
			var line = {
				start: this._simpleScalePoint(this.previousPoint, this.boxScale),
				end: this._simpleScalePoint(point, this.boxScale)
			};

			Draw.addLine(this.page, line);
			Draw.drawLine(element, line);
		}

		Draw.addBreak(this.page);
		this.isMoving = false;
	},

	doMove: function(element, point) {
		this.isMoving = true;
		var $this = $(element);
		point = {x: point.x - $this.offset().left, y: point.y - $this.offset().top};

		var boundingBox = {
			left: 0,
			top: 0,
			width: element.width * this.boxScale,
			height: element.height * this.boxScale
		};

		// If the mouse/finger has strayed outside of the canvas, end the current line and return
		if (!Draw.isPointInsideBox(point, boundingBox)) {
			Draw.addBreak(this.page);
			this.previousPoint = null;
			return;
		} else if (this.previousPoint) {
			// if the previousPoint is set, add a line
			var line = {
				start: this._simpleScalePoint(this.previousPoint, this.boxScale),
				end: this._simpleScalePoint(point, this.boxScale)
			};

			Draw.addLine(this.page, line);
			Draw.drawLine(element, line);
		}

		this.previousPoint = point;
	},

	signBox: function(targetCanvas, page, highlight) {
		var self = this;
		this.page.lines = [];
		this.page.background = null;
		this.page.background = page.background;

		// Calculate the real size of the highlight box in case the user clicked close to the edge of the screen.
		var bottomOfHighlight = highlight.top + highlight.height;
		var left = Math.max(highlight.left, 0);
		var top = Math.max(highlight.top, 0);
		var width = Math.min(highlight.width, page.background.width - left);
		var height = Math.min(Math.min(bottomOfHighlight, highlight.height), page.background.height - top);
		this.boxScale = (this.$main.width() / (width + 30));

		this.page.highlight = {
			left: left,
			top: top,
			width: width,
			height: height
		};

		if (this.boxScale * height > this.$main.height()) {
			this.boxScale = (this.$main.height() / (height + 80));
		}

		this.page.scale = this.boxScale;

		$('body').append('<div id="sign-box"></div>');
		var $signDialog = $('#sign-box');
		$signDialog.append('<canvas id="sign-canvas"></canvas>');

		var signCanvas = uu.canvas.create(width, height, 'vml', uu.id('sign-canvas'));
		// uu.create.canvas removes the id of the placeholder for some reason so we have to reset it.
		$(signCanvas).attr('id', 'sign-canvas');

		Draw.draw(signCanvas, this.page);

		$signDialog.dialog({
			buttons: {
				// TODO i18n text
				Save: function() {
					$(this).dialog('close');
				},
				Undo: function() {
					Draw.undo(signCanvas, self.page);
				},
				Cancel: function() {
					self.page.lines = [];
					$(this).dialog('close');
				}
			},
			close: function(event, ui) {
				self.trackingTouchId = null;
				$(this).remove();

				// If the close button was clicked, treat it like a cancel.
				if (event.originalEvent && event.originalEvent.currentTarget && $(event.originalEvent.currentTarget).is('.ui-dialog-titlebar-close')) {
					self.page.lines = [];
				}

				$.each(self.page.lines, function(index, line) {
					if (line == Draw.LINEBREAK) {
						Draw.addBreak(page);
					} else {
						Draw.addLine(page, {
							start: {
								x: line.start.x + left,
								y: line.start.y + top
							},
							end: {
								x: line.end.x + left,
								y: line.end.y + top
							}
						});
					}
				});

				Draw.draw(targetCanvas, page);
			},
			draggable: !$.support.touch,
			modal: true,
			position: 'top',
			resizable: false,
			width: 'auto'
		});
	},

	_simpleScalePoint: function(point, scale) {
		return {
			x: point.x / scale,
			y: point.y / scale
		};
	},

	init: function() {
		var self = this;

		this.$main = $('#main');

		if ($.support.touch) {
			$('#sign-canvas').live('touchstart', function(event) {
				var e = event.originalEvent;

				if (self.trackingTouchId === null) {
					var $this = $(this);
					var touch = e.targetTouches[0];

					self.trackingTouchId = touch.identifier;
					var point = Draw.convertEventToPoint(touch);
					self.previousPoint = {x: point.x - $this.offset().left, y: point.y - $this.offset().top};
				}
			}).live('touchmove', function(event) {
				var e = event.originalEvent;

				var currentTouch = null;
				for (var i = 0; i < e.touches.length; i++) {
					if (self.trackingTouchId == e.touches[i].identifier) {
						currentTouch = e.touches[i];
					}
				}

				if (currentTouch) {
					var point = Draw.convertEventToPoint(currentTouch);
					self.doMove(this, point);
				}
			}).live('touchend touchcancel', function(event) {
				var e = event.originalEvent;

				var currentTouch = null;
				for (var i = 0; i < e.changedTouches.length; i++) {
					if (self.trackingTouchId == e.changedTouches[i].identifier) {
						currentTouch = e.changedTouches[i];
					}
				}

				if (currentTouch) {
					var point = Draw.convertEventToPoint(currentTouch);
					self.doEnd(this, point);
				}
			});
		} else {
			$('#sign-canvas').live('mousedown', function(e) {
				if (e.which == 1) {
					var $this = $(this);

					self.isMouseDown = true;
					var point = Draw.convertEventToPoint(e);
					self.previousPoint = {x: point.x - $this.offset().left, y: point.y - $this.offset().top};
				}
			}).live('mousemove', function(e) {
				if (self.isMouseDown) {
					var point = Draw.convertEventToPoint(e);

					self.doMove(this, point);
				}
			}).live('mouseup mouseleave', function(e) {
				if (self.isMouseDown) {
					var point = Draw.convertEventToPoint(e);
					self.doEnd(this, point);
				}
			});
		}
	}
};
