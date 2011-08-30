var Sign = {
	//global objects
	$box: null,
	can: null,
	currentPageNumber: null,
	highlightStart: null,
	initialZoom: null,
	$main: null,
	minVisible: 150,
	mtouch: false,
	ORIGIN: {x:0, y:0},
	tapAndHoldTimeout: null,
	tapAndHoldDuration: 500,
	urls: {},

	// Make sure at least 150 x 150 pixels of the screen are visible at any time. 
	canScroll: function(canvas, x, y) {
		var $canvas = $(canvas);
		if (($canvas.width() + x) < this.minVisible) {
			return false;
		} else if (($canvas.height() + y) < this.minVisible) {
			return false;
		} else if (this.$main.width() - x < this.minVisible) {
			return false;
		} else if (this.$main.height() - y < this.minVisible) {
			return false;
		}

		return true;
	},

	_currentCenter: function(page) {
		var left = 0;
		var top = 0;
		var width = 0;
		var height = 0;

		left = -page.scrollCanX / page.scale
		top = -page.scrollCanY / page.scale;

		width = left + (this.$main.width() / page.scale);
		height = top + (this.$main.height() / page.scale);

		return {x: (left + width) / 2, y: (top + height) / 2};
	},

	currentPage: function() {
		return Document.pages[this.currentPageNumber];
	},

	doEnd: function(event) {
		var canvas = this.can;
		var page = this.currentPage();
		clearTimeout(this.tapAndHoldTimeout);

		if (InputHandler.mtouch) {
			return;
		}

		var isSigning = $('#pen').is('.ui-state-highlight');
		var isHighlighting = Party.isHighlighting();

		if (!isSigning && !InputHandler.isMoving && !isHighlighting) {
			var party = Party.getSelectedPartyRow().attr('id') || SignBox.partyName;
			var color = Party.getCurrentColor() || SignBox.partyColor;
			var point = Draw.scalePoint(page, Draw.convertEventToPoint(event));
			var insideHighlight = Draw.isPointInsideAnyBox(point, page.unsavedHighlights[party]);

			if (insideHighlight) {
				SignBox.signBox(canvas, page, insideHighlight);
			} else {
				var boxWidth = (canvas.width * SignBox.widthScale);
				var boxHeight = boxWidth * SignBox.heightScale;
				var corners = {
					left: point.x - SignBox.touchOffset,
					top: point.y - boxHeight + SignBox.touchOffset,
					width: boxWidth,
					height: boxHeight
				}

				Draw.addHighlight(page, party, corners);
				Draw.highlight(canvas, page, corners, color);
			}
		} else if (isSigning) {
			Draw.addBreak(page);
		} else if (isHighlighting && this.highlightStart) {
			var corners = Draw.normalizeCorners(Draw.scalePoint(page, this.highlightStart), Draw.scalePoint(page, InputHandler.previousPoint));
			var party = Party.getSelectedPartyRow().attr('id');
			var color = Party.getCurrentColor();

			Draw.addHighlight(page, party, corners);
			Draw.highlight(canvas, page, corners, color);

			// Reset highlighting
			this.highlightStart = null;
			this.$box.hide().width(0).height(0);
		} else if (!this.highlightStart) {
			this.$main.css('cursor', 'default');
		}
	},

	doGestureChange: function(event) {
		if (InputHandler.mtouch) {
			var e = event.originalEvent;
			$('#slider').slider('value', this.initialZoom * e.scale);
		}
	},

	doMove: function(event) {
		var canvas = this.can;
		var page = this.currentPage();
		clearTimeout(this.tapAndHoldTimeout);

		var point = Draw.convertEventToPoint(event);
		var isSigning = $('#pen').is('.ui-state-highlight');
		var isHighlighting = Party.isHighlighting();

		if (!isSigning && !isHighlighting) {
			this.$main.css('cursor', 'move');
			this.dragCanvas(canvas, page, InputHandler.previousPoint, point);
		} else if (isSigning) {
			var line = {
				start: Draw.scalePoint(page, InputHandler.previousPoint),
				end: Draw.scalePoint(page, point),
			};
			Draw.addLine(page, line);
			Draw.drawLine(canvas, line);
		} else if (isHighlighting) {
			if (!this.highlightStart) {
				var color = Party.getCurrentColor();
				this.$box.show();
				this.$box.css('border', '1px solid ' + color);
				this.$box.css('background-color', color);
				this.highlightStart = point;
			} else {
				this.drawBox(canvas, page, this.highlightStart, point);
			}
		}

		InputHandler.previousPoint = point;
	},

	doStart: function(event, canvas, page) {
		var self = this;
		var canvas = this.can;
		var page = this.currentPage();
		this.initialZoom = $('#slider').slider('value');

		/**
		 * If the user taps/clicks and holds, see if they are over a highlight and then delete it.
		 */
		this.tapAndHoldTimeout = setTimeout(function() {
			if (InputHandler.mtouch) {
				return;
			}

			InputHandler.isMouseDown = false;
			InputHandler.trackingTouchId = null;

			var party = Party.getSelectedPartyRow().attr('id') || SignBox.partyName;
			var color = Party.getCurrentColor() || SignBox.partyColor;
			var point = Draw.scalePoint(page, InputHandler.previousPoint);
			var highlight = Draw.isPointInsideAnyBox(point, page.unsavedHighlights[party]);

			if (highlight) {
				var index = page.unsavedHighlights[party].indexOf(highlight);
				page.unsavedHighlights[party].splice(index, 1);
				Draw.draw(canvas, page);
			}
		}, this.tapAndHoldDuration);
	},

	dragCanvas: function(canvas, page, oldPoint, newPoint) {
		var newScrollCanX = page.scrollCanX + newPoint.x - oldPoint.x;
		var newScrollCanY = page.scrollCanY + newPoint.y - oldPoint.y;

		if (this.canScroll(canvas, newScrollCanX, newScrollCanY)) {
			page.scrollCanX = newScrollCanX;
			page.scrollCanY = newScrollCanY;
			this._transform(canvas, page.scrollCanX, page.scrollCanY);
		}
	},

	drawBox: function(canvas, page, point1, point2) {
		var corners = Draw.normalizeCorners(point1, point2);

		this.$box.offset({left:corners.left, top:corners.top});
		this.$box.width(corners.width);
		this.$box.height(corners.height);
	},

	_realSetupCanvas: function(canvas, page) {
		this.currentPageNumber = page.pageNumber;

		$('#right-arrow a').attr('href', '#' + Math.min(Document.pageCount, page.pageNumber + 1));
		$('#left-arrow a').attr('href', '#' + Math.max(Document.FIRST_PAGE, page.pageNumber - 1));

		$('.arrow').removeClass('ui-state-disabled disabled');

		if (page.pageNumber == 1) {
			$('#left-arrow').addClass('ui-state-disabled disabled');
		}

		if (page.pageNumber == Document.pageCount) {
			$('#right-arrow').addClass('ui-state-disabled disabled');
		}

		canvas.width = page.background.width;
		canvas.height = page.background.height;

		$('#page-number').text(this.currentPageNumber + '/' + Document.pageCount);

		this._zoomEvent(canvas, page, $('#slider').slider('value'));
		var $canvas = $(canvas);
		var headerHeight = $('#logged-in-user').outerHeight(true) + $('#button-panel').outerHeight(true);
		// center the canvas.
		this.dragCanvas(canvas, page,
				{x: page.scrollCanX, y: page.scrollCanY},
				{x: (this.$main.width() - $canvas.width()) / 2, y: headerHeight});
		Draw.draw(canvas, page);
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
					height: round(page.unsavedHighlights[party][i].height * scaleY),
					left: round(page.unsavedHighlights[party][i].left * scaleX),
					width: round(page.unsavedHighlights[party][i].width * scaleX),
					top: round(page.unsavedHighlights[party][i].top * scaleY)
				};
			}
		}

		return scaledPage;
	},

	setupCanvas: function(canvas, page) {
		var self = this;
		if (page.background.complete) {
			this._realSetupCanvas(canvas, page);
		} else {
			bindEvent(page.background, 'load', function() {
				self._realSetupCanvas(canvas, page);
			});
		}
	},

	_transform: function(element, x, y) {
		element.style.webkitTransform = 'translate(' + x + 'px, ' + y + 'px)';
		element.style.MozTransform = 'translate(' + x + 'px, ' + y + 'px)';
		element.style.transform = 'translate(' + x + 'px, ' + y + 'px)';
		element.style.OTransform = 'translate(' + x + 'px, ' + y + 'px)';
		element.style.msTransform = 'translate(' + x + 'px, ' + y + 'px)';
	},

	// Given opposite corners of a rectangle, zoom the screen to that area.
	viewArea: function(canvas, page, point1, point2, scaleBy) {
		var corners = Draw.normalizeCorners(point1, point2);

		var newWidth = corners.width;
		var newHeight = corners.height;

		if (scaleBy == 'width') {
			page.scale = this.$main.width() / newWidth;
		} else {
			page.scale = this.$main.height() / newHeight;
		}

		page.scrollCanX = -corners.left * page.scale;
		page.scrollCanY = -corners.top * page.scale;

		var $canvas = $(canvas);
		$canvas.width(page.background.width * page.scale);
		$canvas.height(page.background.height * page.scale);

		this._transform(canvas, page.scrollCanX, page.scrollCanY);
	},

	_zoomEvent: function(canvas, page, zoom) {
		var zoomScale = 1 / zoom;

		var point = this._currentCenter(page);

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

		this.viewArea(canvas, page, zoomStart, zoomEnd, 'width');
	},

	// document.js functions
	submitLines: function() {
		var self = this;
		var lines = {};

		$.each(Document.pages, function(index, element) {
			lines[index] = Draw.scaleLines(element);
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
		var $can = $(this.can);
		this.$box.hide();
		var eventType = $.support.touch ? 'touchend' : 'click';

		if ($.support.touch) {
			$('#slider-container').hide();
		}

		if (navigator.userAgent.match(/iphone|android/i)) {
			$('h1', '#header').slideUp('fast');
			$('head').append('<link>');
			// TODO: Replace this with grails browser detection
			var css = $('head').children(':last').attr({
				rel: 'stylesheet',
				type: 'text/css',
				href: '/document_vault/css/document/iphone.css'
			});
		}

		$('#clearcan').button({
			icons: { primary: 'ui-icon-refresh' }
		}).bind(eventType, function(event) {
			var page = self.currentPage();

			page.lines.splice(0, page.lines.length);
			page.unsavedHighlights[SignBox.partyName] = [];
			Draw.draw(self.can, page);
		});

		$('#undo').button({
			icons: { primary: 'ui-icon-arrowreturnthick-1-w' }
		}).bind(eventType, function(event) {
			Draw.undo(self.can, self.currentPage());
		});

		$('#pen').button({
			icons: { primary: 'ui-icon-pencil' }
		});

		$('#save').button({
			icons: { primary: 'ui-icon-transferthick-e-w' }
		}).bind(eventType, function(event) {
			$('#confirm-submit').dialog('open');
		});

		$('#close').button({
			icons: { primary: 'ui-icon-circle-close' }
		}).bind(eventType, function(event) {
			window.location.href = self.urls.close;
		});

		$('.arrow').bind(eventType, function(event) {
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

			$('.mark').trigger('marked');
		});

		$('#signature-message').dialog({
			autoOpen: false,
			buttons: {},
			closeOnEscape: false,
			draggable: false,
			modal: true,
			open: function(event, ui) {
				$('.ui-dialog-titlebar-close', $(this).parent()).hide();
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
		}).bind(eventType, function(event) {
			Document.print();
		});

		$('#slider').slider({
			min: .25,
			max: 2,
			change: function(event, ui) {
				self._zoomEvent(self.can, self.currentPage(), ui.value);
			},
			step: .25,
			value: .5
		});

		$('#zoom-in').button({
			icons: { primary: 'ui-icon-zoomin' },
			text: false
		}).bind(eventType, function(event) {
			var $slider = $('#slider');
			var value = $slider.slider('value');
			var step = $slider.slider('option', 'step');

			$slider.slider('value', value + step);
		});

		$('#zoom-out').button({
			icons: { primary: 'ui-icon-zoomout' },
			text: false
		}).bind(eventType, function(event) {
			var $slider = $('#slider');
			var value = $slider.slider('value');
			var step = $slider.slider('option', 'step');

			$slider.slider('value', value - step);
		});

		this.$main.bind('mousewheel', function(event, delta) {
			event.wheelDelta > 0 ? $('#zoom-in').click() : $('#zoom-out').click();
		});

		// Load the page indicated by the location hash
		$(window).hashchange();

		InputHandler.init(this, $can);
		SignBox.init();
	}
};
