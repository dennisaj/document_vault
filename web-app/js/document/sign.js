var Sign = {
	//global objects
	$box: null,
	can: null,
	currentHeight: 0,
	currentWidth: 0,
	currentPageNumber: null,
	highlightStart: null,
	highlightOpacity: .7,
	isMoving: false,
	isPainting: false,
	isZoomedIn: false,
	LINEBREAK: 'LB',
	lowlightOpacity: .2,
	$main : null,
	minVisible: 150,
	ORIGIN: {x:0, y:0},
	previousPoint: null,
	scale: 1,
	scrollCanX: 0,
	scrollCanY: 0,
	trackingTouchId: null,
	urls: {},
	ZOOM_SCALE: .3,

	addBreak: function(page) {
		// Don't add consecutive LINEBREAKs
		if (page.lines[page.lines.length - 1] != this.LINEBREAK) {
			page.lines.push(this.LINEBREAK);
		}
	},

	_addLine: function(page, line) {
		page.lines.push(line);
	},

	_addHighlight: function(page, party, corners) {
		page.unsavedHighlights[party] = page.unsavedHighlights[party] || [];
		page.unsavedHighlights[party].push(corners);
	},

	// Make sure at least 150 x 150 pixels of the screen are visible at any time. 
	canScroll: function(x, y) {
		if ((this.currentWidth + x) < this.minVisible) {
			return false;
		} else if ((this.currentHeight + y) < this.minVisible) {
			return false;
		} else if (this.$main.width() - x < this.minVisible) {
			return false;
		} else if (this.$main.height() - y < this.minVisible) {
			return false;
		}

		return true;
	},

	clearCanvas: function(canvas, page) {
		var context = canvas.getContext('2d');

		context.clearRect(0, 0, canvas.width, canvas.height);
		canvas.width = canvas.width;
		context.drawImage(page.background, 0, 0, canvas.width, canvas.height);
	},

	_convertEventToPoint: function(touch) {
		if (touch) {
			return {x: touch.pageX, y: touch.pageY};
		} else {
			return this.ORIGIN;
		}
	},

	_currentCenter: function() {
		var upperLeftCorner = $.extend({}, this.ORIGIN);
		var lowerRightCorner = $.extend({}, this.ORIGIN);

		upperLeftCorner.x = -this.scrollCanX / this.scale
		upperLeftCorner.y = -this.scrollCanY / this.scale;

		lowerRightCorner.x = upperLeftCorner.x + (this.$main.width() / this.scale);
		lowerRightCorner.y = upperLeftCorner.y + (this.$main.height() / this.scale);

		return {x: (upperLeftCorner.x + lowerRightCorner.x) / 2, y: (upperLeftCorner.y + lowerRightCorner.y) / 2};
	},

	currentPage: function() {
		return Document.pages[this.currentPageNumber];
	},

	doEnd: function(event) {
		var isSigning = $('#pen').is('.ui-state-highlight');
		var isHighlighting = Party.isHighlighting();
		$('.arrow').fadeIn('fast');

		if (!isSigning && !this.isMoving && !isHighlighting) {
			// Handle click to zoom
			var zoomScale = this.ZOOM_SCALE;

			var point = this.scalePoint(this._convertEventToPoint(event));

			if (this.isZoomedIn) {
				zoomScale = this.currentPage().background.width / this.$main.width();

				point = this._currentCenter();
			}

			var zoomWidth = zoomScale * this.$main.width();
			var widthOffset = zoomWidth / 2;

			var zoomHeight = zoomScale * this.$main.height();
			var heightOffset = zoomHeight / 2;

			var zoomStart = {
				x: point.x - widthOffset,
				y: point.y - heightOffset
			};

			var zoomEnd = {
				x: point.x + widthOffset, 
				y: point.y + heightOffset
			};

			this.viewArea(this.can, this.currentPage(), zoomStart, zoomEnd, 'width');
			this.isZoomedIn = !this.isZoomedIn;
		} else if (isSigning) {
			this.addBreak(this.currentPage());
		} else if (isHighlighting && this.highlightStart) {
			var corners = this._normalizeCorners(this.scalePoint(this.highlightStart), this.scalePoint(this.previousPoint));
			var party = Party.getSelectedPartyRow().attr('id');
			var color = Party.getCurrentColor();

			this._addHighlight(this.currentPage(), party, corners);
			this.highlight(this.can, this.currentPage(), corners, color);

			// Reset highlighting
			this.highlightStart = null;
			this.$box.hide().width(0).height(0);
		} else if (!this.highlightStart) {
			this.$main.css('cursor', 'default');
		}

		this.isMoving = false;
	},

	doMove: function(event) {
		this.isMoving = true;
		var point = this._convertEventToPoint(event);
		var isSigning = $('#pen').is('.ui-state-highlight');
		var isHighlighting = Party.isHighlighting();
		if ($('.arrow:visible')) {
			$('.arrow').fadeOut('fast');
		}

		if (!isSigning && !isHighlighting) {
			this.$main.css('cursor', 'move');
			this.dragCanvas(this.can, this.previousPoint, point);
		} else if (isSigning) {
			var line = {
				start: this.scalePoint(this.previousPoint),
				end: this.scalePoint(point),
			};
			this._addLine(this.currentPage(), line);
			this.drawLine(this.can, line);
		} else if (isHighlighting) {
			if (!this.highlightStart) {
				var color = Party.getCurrentColor();
				this.$box.show();
				this.$box.css('border', '1px solid ' + color);
				this.$box.css('background-color', color);
				this.highlightStart = point;
			} else {
				this.drawBox(this.can, this.currentPage(), this.highlightStart, point);
			}
		}

		this.previousPoint = point;
	},

	dragCanvas: function(canvas, oldPoint, newPoint) {
		var newScrollCanX = this.scrollCanX + newPoint.x - oldPoint.x;
		var newScrollCanY = this.scrollCanY + newPoint.y - oldPoint.y;

		if (this.canScroll(newScrollCanX, newScrollCanY)) {
			this.scrollCanX = newScrollCanX;
			this.scrollCanY = newScrollCanY;
			this._transform(canvas, this.scrollCanX, this.scrollCanY);
		}
	},

	// Rename me
	draw: function(canvas, page) {
		this.clearCanvas(canvas, page);

		for (var i = 0; i < page.lines.length; i++) {
			if (page.lines[i] == this.LINEBREAK) {
				continue;
			}
			this.drawLine(canvas, page.lines[i]);
		}

		// Only draw the highlights when we are requesting signatures
		if (Party.isRequestingSignatures()) {
			var activePartyId = Party.getSelectedPartyRow().attr('id');

			// Merge saved and unsaved highlights.
			var highlights = $.extend(true, {}, this.currentPage().savedHighlights);
			$.each(this.currentPage().unsavedHighlights, function(key, value) { 
				highlights[key] = $.merge(highlights[key] || [], value);
			});

			for (var party in highlights) {
				// If we are highlighting and this party is the active party or if we are not highlighting, use the dark opacity.
				// Otherwise use the lighter opacity.
				var useHighlight = (party == activePartyId || !Party.isHighlighting());

				for (var i = 0; i < highlights[party].length; i++) {
					this.highlight(canvas, page, highlights[party][i], Party.getPartyColor(party), useHighlight ? this.highlightOpacity : this.lowlightOpacity);
				}
			}
		}
	},

	drawLine: function(canvas, line, strokeStyle) {
		var context = canvas.getContext('2d');
		strokeStyle = strokeStyle || 'rgba(0, 128, 0, 1)';

		context.strokeStyle = strokeStyle;
		context.lineJoin = 'round';
		context.lineWidth = 1.5;
		context.beginPath();
		context.moveTo(line.start.x, line.start.y);
		context.lineTo(line.end.x, line.end.y);
		context.closePath();
		context.stroke();
	},

	drawBox: function(canvas, page, point1, point2) {
		var corners = this._normalizeCorners(point1, point2);

		this.$box.offset({left:corners.upperLeftCorner.x, top:corners.upperLeftCorner.y});
		this.$box.width(corners.lowerRightCorner.x - corners.upperLeftCorner.x);
		this.$box.height(corners.lowerRightCorner.y - corners.upperLeftCorner.y);
	},

	highlight: function(canvas, page, corners, color, opacity) {
		// If the color isn't set, the highlight will be invisible
		color = color || "rgba(255, 255, 255, 0)";
		opacity = opacity || this.highlightOpacity;

		var context = canvas.getContext('2d');
		var width = corners.lowerRightCorner.x - corners.upperLeftCorner.x;
		var height = corners.lowerRightCorner.y - corners.upperLeftCorner.y;

		context.fillStyle = color;
		context.globalAlpha = opacity;
		context.fillRect(corners.upperLeftCorner.x, corners.upperLeftCorner.y, width, height);
	},

	_normalizeCorners: function(point1, point2) {
		return {
			upperLeftCorner: {
				x: Math.min(point1.x, point2.x), 
				y: Math.min(point1.y, point2.y)
			},

			lowerRightCorner: {
				x: Math.max(point1.x, point2.x), 
				y: Math.max(point1.y, point2.y)
			}
		}
	},

	realSetupCanvas: function(canvas, page) {
		this.currentPageNumber = page.pageNumber;

		$('#right-arrow a').attr('href', '#' + Math.min(Document.pageCount, page.pageNumber + 1));
		$('#left-arrow a').attr('href', '#' + Math.max(Document.FIRST_PAGE, page.pageNumber - 1));

		$('.arrow a').removeClass('disabled ui-state-disabled');

		if (page.pageNumber == 1) {
			$('#left-arrow a').addClass('disabled ui-state-disabled');
		}

		if (page.pageNumber == Document.pageCount) {
			$('#right-arrow a').addClass('disabled ui-state-disabled');
		}

		canvas.width = page.background.width;
		canvas.height = page.background.height;

		$('#page-number').text(this.currentPageNumber + '/' + Document.pageCount);

		this.viewArea(canvas, page, this.ORIGIN, {x:page.background.width, y:page.background.height}, 'width');
		this.draw(canvas, page);
	},

	_round: function(number, places) {
		places = places || 1
		return parseFloat(number.toFixed(places));
	},

	_scaleCorners: function(page) {
		if (!page || !page.unsavedHighlights) {
			return {};
		}

		var scaleX = page.sourceWidth / page.background.width;
		var scaleY = page.sourceHeight / page.background.height;
		var scaledPage = {};

		for (var party in page.unsavedHighlights) {
			scaledPage[party] = [];

			for (var i = 0; i < page.unsavedHighlights[party].length; i++) {
				scaledPage[party][i] = {
					a: {
						x:this._round(page.unsavedHighlights[party][i].upperLeftCorner.x * scaleX),
						y:this._round(page.unsavedHighlights[party][i].upperLeftCorner.y * scaleY)
					},
					b: {
						x:this._round(page.unsavedHighlights[party][i].lowerRightCorner.x * scaleX),
						y:this._round(page.unsavedHighlights[party][i].lowerRightCorner.y * scaleY)
					}
				};
			}
		}

		return scaledPage;
	},

	_scaleLines: function(page) {
		if (!page || !page.lines || !page.lines.length) {
			return {};
		}

		var scaleX = page.sourceWidth / page.background.width;
		var scaleY = page.sourceHeight / page.background.height;
		var lines = [];

		for (var i = 0; i < page.lines.length; i++) {
			if (page.lines[i] == this.LINEBREAK) {
				lines[i] = this.LINEBREAK;
				continue;
			}

			lines[i] = {
				a: {
					x:this._round(page.lines[i].start.x * scaleX),
					y:this._round(page.lines[i].start.y * scaleY)
				},
				b: {
					x:this._round(page.lines[i].end.x * scaleX),
					y:this._round(page.lines[i].end.y * scaleY)
				}
			};
		}

		return lines;
	},

	scalePoint: function(point) {
		return {
			x: (point.x - this.scrollCanX) / this.scale, 
			y: (point.y - this.scrollCanY) / this.scale
		}
	},

	setupCanvas: function(canvas, page) {
		var self = this;
		if (page.background.complete) {
			this.realSetupCanvas(canvas, page);
		} else {
			page.background.addEventListener('load', function() {
				self.realSetupCanvas(canvas, page);
			}, false);
		}
	},

	_transform: function(element, x, y) {
		element.style.webkitTransform = 'translate(' + x + 'px, ' + y + 'px)';
		element.style.MozTransform = 'translate(' + x + 'px, ' + y + 'px)';
		element.style.transform = 'translate(' + x + 'px, ' + y + 'px)';
		element.style.OTransform = 'translate(' + x + 'px, ' + y + 'px)';
	},
	
	// Given opposite corners of a rectangle, zoom the screen to that area.
	viewArea: function(canvas, page, point1, point2, scaleBy, center) {
		var corners = this._normalizeCorners(point1, point2);

		var newWidth = corners.lowerRightCorner.x - corners.upperLeftCorner.x;
		var newHeight = corners.lowerRightCorner.y - corners.upperLeftCorner.y;

		if (scaleBy == 'width') {
			this.scale = this.$main.width() / newWidth;
		} else {
			this.scale = this.$main.height() / newHeight;
		}

		this.scrollCanX = -corners.upperLeftCorner.x * this.scale;
		this.scrollCanY = -corners.upperLeftCorner.y * this.scale;

		this.currentWidth = page.background.width * this.scale;
		this.currentHeight = page.background.height * this.scale;
		canvas.style.width = this.currentWidth + 'px';
		canvas.style.height = this.currentHeight + 'px';

		this._transform(canvas, this.scrollCanX, this.scrollCanY);
	},

	// document.js functions
	submitLines: function() {
		var self = this;
		var lines = {}

		$.each(Document.pages, function(index, element) {
			lines[index] = self._scaleLines(element);
		});

		Document.submitLines(lines).then(function() {
			$('#signature-message').dialog('close');
			window.location.href = self.urls.finish_redirect;
		});
	},
	// !document.js functions

	init: function(urls) {
		var self = this;
		this.urls = urls;

		this.$main = $('#main');
		this.$box = $('#box');
		this.can = document.getElementById('can');
		this.previousPoint = this.ORIGIN;
		this.$box.hide();

		if ($.support.touch) {
			this.can.ontouchstart = function(e) {
				if (self.trackingTouchId == null) {
					self.trackingTouchId = e.touches[0].identifier;

					self.previousPoint = self._convertEventToPoint(e.touches[0]);
				}
			};

			this.can.ontouchmove = function(e) {
				var currentTouch = null;
				for (var i = 0; i < e.touches.length; i++) {
					if (self.trackingTouchId == e.touches[i].identifier) {
						currentTouch = e.touches[i];
					}
				}
	
				if (currentTouch) {
					self.doMove(currentTouch);
				}
			};

			this.can.ontouchend = function(e) {
				var currentTouch = null;
				for (var i = 0; i < e.changedTouches.length; i++) {
					if (self.trackingTouchId == e.changedTouches[i].identifier) {
						currentTouch = e.changedTouches[i];
					}
				}

				if (currentTouch) {
					self.trackingTouchId = null;
					self.doEnd(currentTouch);
				}
			};
		} else {
			$('#can').mousedown(function(e) {
				if (e.which == 1) {
					self.isPainting = true;
					self.previousPoint = {x: e.pageX, y: e.pageY};
				}
			});

			$('#can, #box').mousemove(function(e) {
				if (self.isPainting) {
					self.doMove(e);
				}
			});

			$('#can, #box').mouseup(function(e) {
				if (self.isPainting) {
					self.doEnd(e);
					self.isPainting = false;
				}
			});

			$('#can').mouseleave(function(e) {
				if (e.toElement.id != 'box' && self.isPainting) {
					self.doEnd(e);
					self.isPainting = false;
				}
			});
		}

		if (navigator.userAgent.match(/iphone|android/i)) {
			$('h1', '#header').slideUp('fast');
			$("head").append("<link>");
			// TODO: Replace this with grails browser detection
			var css = $("head").children(":last").attr({
				rel: "stylesheet",
				type: "text/css",
				href: "/document_vault/css/document/iphone.css"
			});
		}

		$('#clearcan').button({
			icons: { primary: 'ui-icon-refresh' }
		}).click(function() {
			self.currentPage().lines.splice(0, self.currentPage().lines.length);
			self.draw(self.can, self.currentPage());
		});

		$('#undo').button({
			icons: { primary: 'ui-icon-arrowreturnthick-1-w' }
		}).click(function() {
			var splicePoint = self.currentPage().lines.length;
			for (var i = self.currentPage().lines.length - 2; i >= 0; i--) {
				if (self.currentPage().lines[i] == self.LINEBREAK || i == 0) {
					splicePoint = i;
					break;
				}
			}
			self.currentPage().lines.splice(splicePoint, self.currentPage().lines.length);
			self.addBreak(self.currentPage());
			self.draw(self.can, self.currentPage());
		});

		$('#pen').button({
			icons: { primary: 'ui-icon-pencil' }
		});

		$('#save').button({
			icons: { primary: 'ui-icon-transferthick-e-w' }
		}).click(function() {
			$('#confirm-submit').dialog('open');
		});

		$('#close').button({
			icons: { primary: 'ui-icon-circle-close' }
		}).click(function() {
			window.location.href = self.urls.close;
		});

		$('#zoomWidth').button({
			icons: { primary: 'ui-icon-arrowthick-2-e-w' }
		}).click(function() {
			self.viewArea(self.can, self.currentPage(), self.ORIGIN, {x:self.currentPage().background.width, y:self.currentPage().background.height}, 'width');
			self.isZoomedIn = false;
		});

		$('#left-arrow a').button({
			icons: { primary: 'ui-icon-circle-arrow-w' },
			text: false
		});

		$('#right-arrow a').button({
			icons: { primary: 'ui-icon-circle-arrow-e' },
			text: false
		});

		$('.arrow a').click(function() {
			if ($(this).is('.disabled')) {
				return false;
			}
		});

		$(window).hashchange(function(event) {
			var newPage = parseInt(location.hash.substring(1)) || Document.FIRST_PAGE;

			if (!self.currentPage() || newPage != self.currentPageNumber) {
				Document.getPage(newPage, function(page) {
					self.setupCanvas(self.can, page);
				});
			}
		});

		window.onorientationchange = function() {
			window.scrollTo(0, 1);
			if (!self.isZoomedIn) {
				$('#zoomWidth').click();
			}
		};

		$('.mark').click(function(e) {
			var $this = $(this);
			var wasOn = $this.is('.ui-state-highlight');

			$('.mark').removeClass('ui-state-highlight');

			if (!wasOn) {
				$this.toggleClass('ui-state-highlight');
			}

			if ($this.is('.ui-state-highlight')) {
				self.$main.css('cursor', 'crosshair');
			} else {
				self.$main.css('cursor', 'default');
			}
		});

		$('#signature-message').dialog({
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

		$('#confirm-submit').dialog({
			autoOpen: false,
			buttons: {
				'Submit': function() {
					$(this).dialog('close');
					$('#signature-message').dialog('open');
					self.submitLines();
				},
				'Cancel' :function() {
					$(this).dialog('close');
				}
			},
			draggable: false,
			modal: true,
			resizable: false
		});

		Party.init($.extend({}, this.urls));

		// Setup document
		Document.init($.extend({}, this.urls));
		$('#print').button({
			icons: { primary: 'ui-icon-print' }
		}).click(function() {
			Document.print();
		});

		// Load the page indicated by the location hash
		$(window).hashchange();
	}
};
