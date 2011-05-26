var Drawing = {
	//global objects
	$box: null,
	can: null,
	currentHeight: 0,
	currentWidth: 0,
	currentPage: null,
	FIRST_PAGE: 1,
	highlightStart: null,
	isMoving: false,
	isPainting: false,
	isZoomedIn: false,
	LINEBREAK: 'LB',
	$main : null,
	minVisible: 150,
	ORIGIN: {x:0, y:0},
	previousPoint: null,
	pageCount: 0,
	pages: null,
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

	addLine: function(page, line) {
		page.lines.push(line);
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

	convertEventToPoint: function(touch) {
		if (touch) {
			return {x: touch.pageX, y: touch.pageY};
		} else {
			return this.ORIGIN;
		}
	},

	currentCenter: function() {
		var upperLeftCorner = $.extend({}, this.ORIGIN);
		var lowerRightCorner = $.extend({}, this.ORIGIN);

		upperLeftCorner.x = -this.scrollCanX / this.scale
		upperLeftCorner.y = -this.scrollCanY / this.scale;

		lowerRightCorner.x = upperLeftCorner.x + (this.$main.width() / this.scale);
		lowerRightCorner.y = upperLeftCorner.y + (this.$main.height() / this.scale);

		return {x: (upperLeftCorner.x + lowerRightCorner.x) / 2, y: (upperLeftCorner.y + lowerRightCorner.y) / 2};
	},

	doEnd: function(event) {
		var isDrawing = $('#pen').is('.on');
		var isHighlighting = $('#highlight').is('.on');
		$('.arrow').fadeIn('fast');

		if (!isDrawing && !this.isMoving && !isHighlighting) {
			var zoomScale = this.ZOOM_SCALE;

			var point = this.scalePoint(this.convertEventToPoint(event));

			if (this.isZoomedIn) {
				zoomScale = this.currentPage.background.width / this.$main.width();

				point = this.currentCenter();
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

			this.viewArea(this.can, this.currentPage, zoomStart, zoomEnd, 'width');
			this.isZoomedIn = !this.isZoomedIn;
		} else if (isDrawing) {
			this.addBreak(this.currentPage);
		} else if (isHighlighting && this.highlightStart) {
			this.$box.hide().width(0).height(0);
			this.highlight(this.can, this.currentPage, this.scalePoint(this.highlightStart), this.scalePoint(this.previousPoint));
			this.highlightStart = null;
		} else {
			this.$main.css('cursor', 'default');
		}

		this.isMoving = false;
	},

	doMove: function(event) {
		this.isMoving = true;
		var point = this.convertEventToPoint(event);
		var isDrawing = $('#pen').is('.on');
		var isHighlighting = $('#highlight').is('.on');
		if ($('.arrow:visible')) {
			$('.arrow').fadeOut('fast');
		}

		if (!isDrawing && !isHighlighting) {
			this.$main.css('cursor', 'move');
			this.dragCanvas(this.can, this.previousPoint, point);
		} else if (isDrawing) {
			var line = {
				start: this.scalePoint(this.previousPoint),
				end: this.scalePoint(point),
			};
			this.addLine(this.currentPage, line);
			this.drawLine(this.can, line);
		} else if (isHighlighting) {
			if (!this.highlightStart) {
				this.$box.show();
				this.highlightStart = point;
			} else {
				this.drawBox(this.can, this.currentPage, this.highlightStart, point);
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
			canvas.style.webkitTransform = 'translate(' + this.scrollCanX + 'px, ' + this.scrollCanY + 'px)';
			canvas.style.MozTransform = 'translate(' + this.scrollCanX + 'px, ' + this.scrollCanY + 'px)';
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

	drawBox: function(canvas, page, point1, point2, color) {
		color = color || "rgba(255, 255, 0, 0.7)";
		var corners = this._normalizeCorners(point1, point2);

		this.$box.css('border', '1px solid ' + color);
		this.$box.css('background', color);
		this.$box.offset({left:corners.upperLeftCorner.x, top:corners.upperLeftCorner.y});
		this.$box.width(corners.lowerRightCorner.x - corners.upperLeftCorner.x);
		this.$box.height(corners.lowerRightCorner.y - corners.upperLeftCorner.y);
	},

	highlight: function(canvas, page, point1, point2, color) {
		color = color || "rgba(255, 255, 0, 0.7)";

		var context = canvas.getContext('2d');
		var corners = this._normalizeCorners(point1, point2);
		var width = corners.lowerRightCorner.x - corners.upperLeftCorner.x;
		var height = corners.lowerRightCorner.y - corners.upperLeftCorner.y;

		this.draw(canvas, page);
		context.fillStyle = color;
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
	_round: function(number, places) {
		places = places || 1
		return parseFloat(number.toFixed(places));
	},

	realSetupCanvas: function(canvas, page) {
		this.currentPage = page;

		$('#right-arrow a').attr('href', '#' + Math.min(this.pageCount, page.pageNumber + 1));
		$('#left-arrow a').attr('href', '#' + Math.max(1, page.pageNumber - 1));

		$('.arrow a').removeClass('disabled');

		if (page.pageNumber == 1) {
			$('#left-arrow a').addClass('disabled');
		}

		if (page.pageNumber == this.pageCount) {
			$('#right-arrow a').addClass('disabled');
		}

		canvas.width = page.background.width;
		canvas.height = page.background.height;

		this.viewArea(canvas, page, this.ORIGIN, {x:page.background.width, y:page.background.height}, 'width');
		this.draw(canvas, page);
	},

	scaleLines: function(page) {
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
		if (page.background.complete) {
			this.realSetupCanvas(canvas, page)
		} else {
			page.background.onload = function() {
				Drawing.realSetupCanvas(canvas, page)
			};
		}
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

		canvas.style.webkitTransform = 'translate(' + this.scrollCanX + 'px, ' + this.scrollCanY + 'px)';
		canvas.style.MozTransform = 'translate(' + this.scrollCanX + 'px, ' + this.scrollCanY + 'px)';
	},

	onAjaxError: function(jqXHR, textStatus, errorThrown) {
		$('#dialog-message').dialog('close');

		HtmlAlert._alert('An error has occurred', '<p><span class="ui-icon ui-icon-alert" style="float: left; margin: 0 7px 50px 0;"></span>There was an error communicating with the server. Please try again.</p>');
	},

	// Document functions
	getPage: function(canvas, documentId, pageNumber) {
		if (pageNumber > this.pageCount) {
			pageNumber = this.pageCount;
		} else if (pageNumber < this.FIRST_PAGE) {
			pageNumber = this.FIRST_PAGE;
		}

		if (this.pages[pageNumber]) {
			this.setupCanvas(canvas, this.pages[pageNumber]);
		} else {
			Document.getPage(documentId, pageNumber, function(page) {
				Drawing.pages[page.pageNumber] = page;
				Drawing.setupCanvas(Drawing.can, Drawing.pages[page.pageNumber]);
			});
		}
	},

	submitPages: function(documentId) {
		var lines = {}
		$.each(Drawing.pages, function(index, element) {
			lines[index] = Drawing.scaleLines(element);
		});

		Document.submitPages(documentId, lines, function() {
			$('#dialog-message').dialog('close');
			window.location.href = Drawing.urls['finish_redirect'];
		});
	},
	// !Document functions

	init: function(urls) {
		var self = this;
		this.urls = urls;

		this.$main = $('#main');
		this.$box = $('#box');
		this.can = document.getElementById('can');
		this.previousPoint = this.ORIGIN;
		this.pageCount = parseInt($('#pageCount').val() || this.FIRST_PAGE);
		this.pages = new Array(this.pageCount + this.FIRST_PAGE);
		this.$box.hide();

		if ($.support.touch) {
			this.can.ontouchstart = function(e) {
				if (self.trackingTouchId == null) {
					self.trackingTouchId = e.touches[0].identifier;

					self.previousPoint = self.convertEventToPoint(e.touches[0]);
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
				href: "/document_vault/css/iphone.css"
			});
		}

		$('#clearcan').click(function() {
			self.currentPage.lines.splice(0, self.currentPage.lines.length);
			self.draw(self.can, self.currentPage);
		});

		$('#undo').click(function() {
			var splicePoint = self.currentPage.lines.length;
			for (var i = self.currentPage.lines.length - 2; i >= 0; i--) {
				if (self.currentPage.lines[i] == self.LINEBREAK || i == 0) {
					splicePoint = i;
					break;
				}
			}
			self.currentPage.lines.splice(splicePoint, self.currentPage.lines.length);
			self.addBreak(self.currentPage);
			self.draw(self.can, self.currentPage);
		});

		$('#save').click(function() {
			$('#confirm-submit').dialog('open');
		});

		$('#close').click(function() {
			window.location.href = self.urls['close'];
		});

		$('.arrow a').click(function() {
			if ($(this).is('.disabled')) {
				return false;
			}
		});

		$(window).hashchange(function() {
			var newPage = parseInt(location.hash.substring(1)) || self.FIRST_PAGE;

			if (!self.currentPage || newPage != self.currentPage.pageNumber) {
				self.getPage(self.can, $('#documentId').val(), newPage);
			}
		});

		window.onorientationchange = function() {
			window.scrollTo(0, 1);
			if (!self.isZoomedIn) {
				$('#zoomWidth').click();
			}
		};

		$('#zoomWidth').click(function() {
			self.viewArea(self.can, self.currentPage, self.ORIGIN, {x:self.currentPage.background.width, y:self.currentPage.background.height}, 'width');
			self.isZoomedIn = false;
		});

		$('.mark').click(function(e) {
			var $this = $(this);
			var wasOn = $this.is('.on');

			$('.mark').removeClass('on');

			if (!wasOn) {
				$this.toggleClass('on');
			}

			if ($this.is('.on')) {
				self.$main.css('cursor', 'crosshair');
			} else {
				self.$main.css('cursor', 'default');
			}
		});

		$('#dialog-message').dialog({
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
					$('#dialog-message').dialog('open');
					self.submitPages($('#documentId').val());
				},
				'Cancel' :function() {
					$(this).dialog('close');
				}
			},
			draggable: false,
			modal: true,
			resizable: false
		});

		// Setup document
		Document.init($.extend({}, this.urls), this.onAjaxError);
		$('#print').click(function() {
			Document.print();
		});

		$('#email').click(function() {
			Document.email();
		});

		// Load the page indicated by the location hash
		$(window).hashchange();
	}
};
