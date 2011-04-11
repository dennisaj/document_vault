var Drawing = {
	//global objects
	can: null,
	currentHeight: 0,
	currentWidth: 0,
	currentPage: null,
	LINEBREAK: 'LINEBREAK',
	$main : null,
	isDragging: false,
	isPainting: false,
	isZoomedIn: false,
	minVisible: 150,
	ORIGIN: {x:0, y:0},
	previousPoint: null,
	pages: null,
	scale: 1,
	scrollCanX: 0,
	scrollCanY: 0,
	trackingTouchId: null,
	ZOOM_SCALE: .3,

	_alert: function(title, html) {
		$('#alert').dialog('close');
		
		$('#alert').dialog({
			autoOpen: false,
		    buttons: {
		        'Ok' : function(){
		            $(this).dialog('close');;
		        }
		    },
			closeOnEscape: true,
		    hasClose: false, 
			modal : true, 
			resizable: false,
			title: title
		});
		$('#alert').html(html);
		
		$('#alert').dialog('open');
	},
	
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
		var upperLeftCorner = $.extend({}, Drawing.ORIGIN);
		var lowerRightCorner = $.extend({}, Drawing.ORIGIN);

		upperLeftCorner.x = -this.scrollCanX / this.scale
		upperLeftCorner.y = -this.scrollCanY / this.scale;

		lowerRightCorner.x = upperLeftCorner.x + (this.$main.width() / this.scale);
		lowerRightCorner.y = upperLeftCorner.y + (this.$main.height() / this.scale);

		return {x: (upperLeftCorner.x + lowerRightCorner.x) / 2, y: (upperLeftCorner.y + lowerRightCorner.y) / 2};
	},

	doEnd: function(event) {
		var isDrawing = $('#pen').is('.on');
			
		if (!isDrawing && !this.isDragging) {
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
			this.isZoomedIn = !this.isZoomedIn;//true;
		} else if (isDrawing) {
			this.addBreak(this.currentPage);
		} else {
			this.$main.css('cursor', 'default');
		}
		
		this.isDragging = false;
	},
	
	doMove: function(event) {
		this.isDragging = true;
		var point = this.convertEventToPoint(event);
		var isDrawing = $('#pen').is('.on');
		
		if (!isDrawing) {
			this.$main.css('cursor', 'move');
			this.dragCanvas(this.previousPoint, point);
		} else {
			var line = {
				start: this.scalePoint(this.previousPoint),
				end: this.scalePoint(point),
			};
			this.addLine(this.currentPage, line);
			this.drawLine(this.can, line);
		}
		
		this.previousPoint = point;
	},
	
	dragCanvas: function(oldPoint, newPoint) {
		var newScrollCanX = this.scrollCanX + newPoint.x - oldPoint.x;
		var newScrollCanY = this.scrollCanY + newPoint.y - oldPoint.y;
		
		if (this.canScroll(newScrollCanX, newScrollCanY)) {
			this.scrollCanX = newScrollCanX;
			this.scrollCanY = newScrollCanY;
			this.can.style.webkitTransform = 'translate(' + this.scrollCanX + 'px, ' + this.scrollCanY + 'px)';
			this.can.style.MozTransform = 'translate(' + this.scrollCanX + 'px, ' + this.scrollCanY + 'px)';
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
	
	/**
	 * 
	 * @param page
	 * @returns a base64 encoded png or "data:image/png;base64," if page is null.
	 */
	drawHiddenCanvas: function(page) {
		if (!page || !page.lines.length) {
			return "data:image/png;base64,";
		}
		
		var hiddenCanvas = document.getElementById('hidden-canvas');
		hiddenCanvas.width = page.background.width;
		hiddenCanvas.height = page.background.height;
		
		var scaleX = page.sourceWidth / page.background.width;
		var scaleY = page.sourceHeight / page.background.height;
		
		var hiddenContext = hiddenCanvas.getContext('2d');
		
		hiddenContext.clearRect(0, 0, hiddenCanvas.width, hiddenCanvas.height);
		hiddenContext.scale(scaleX, scaleY);
		hiddenCanvas.width = hiddenCanvas.width;
		
		for (var i = 0; i < page.lines.length; i++) {
			if (page.lines[i] == this.LINEBREAK) {
				continue;
			}
			this.drawLine(hiddenCanvas, page.lines[i], 'rgba(0, 0, 0, 1)');
		}
		
		return hiddenCanvas.toDataURL();
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
	
	realSetupCanvas: function(canvas, page) {
		this.currentPage = page;
				
		$('#right-arrow a').attr('href', '#' + Math.min(this.pages.length - 1, page.pageNumber + 1));
		$('#left-arrow a').attr('href', '#' + Math.max(0, page.pageNumber - 1));
		
		$('.arrow a').removeClass('disabled');
		
		if (page.pageNumber == 0) {
			$('#left-arrow a').addClass('disabled');
		}
		
		if (page.pageNumber == this.pages.length - 1) {
			$('#right-arrow a').addClass('disabled');
		}

		canvas.width = page.background.width;
		canvas.height = page.background.height;
		
		this.viewArea(canvas, page, this.ORIGIN, {x:page.background.width, y:page.background.height}, 'width');
		this.draw(canvas, page);
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
		var upperLeftCorner = {
			x: Math.min(point1.x, point2.x), 
			y: Math.min(point1.y, point2.y)
		};

		var lowerRightCorner = {
			x: Math.max(point1.x, point2.x), 
			y: Math.max(point1.y, point2.y)
		};

		var newWidth = lowerRightCorner.x - upperLeftCorner.x;
		var newHeight = lowerRightCorner.y - upperLeftCorner.y;

		if (scaleBy == 'width') {
			this.scale = this.$main.width() / newWidth;
		} else {
			this.scale = this.$main.height() / newHeight;
		}

		this.scrollCanX = -upperLeftCorner.x * this.scale;
		this.scrollCanY = -upperLeftCorner.y * this.scale;

		this.currentWidth = page.background.width * this.scale;
		this.currentHeight = page.background.height * this.scale;
		canvas.style.width = this.currentWidth + 'px';
		canvas.style.height = this.currentHeight + 'px';

		canvas.style.webkitTransform = 'translate(' + this.scrollCanX + 'px, ' + this.scrollCanY + 'px)';
		canvas.style.MozTransform = 'translate(' + this.scrollCanX + 'px, ' + this.scrollCanY + 'px)';
	},

	onAjaxError: function(jqXHR, textStatus, errorThrown) {
		$('#dialog-message').dialog('close');
	
		this._alert('Unable to save the signatures', '<p><span class="ui-icon ui-icon-alert" style="float: left; margin: 0 7px 50px 0;"></span>Oopsie! ' + textStatus + '</p>');
	},

	// AJAX stuff
	finishDocument: function(documentId) {
		var $dialog = $('#dialog-message');
		$.ajax({
			beforeSend: function() {/* Add throbber */ },
			complete: function() {/* Remove throbber */ },
			error: this.onAjaxError,
			global: false,
			success: function(data) {
				$dialog.dialog('close');

				window.location.href = "/document_vault/document/index";
			},
			type: 'GET',
			url: '/document_vault/document/finish/' + documentId
		});
	},
	
	getPage: function(canvas, documentId, pageNumber) {
		if (pageNumber >= this.pages.length) {
			pageNumber = this.pages.length - 1;
		} else if (pageNumber < 0) {
			pageNumber = 0;
		}
		
		if (this.pages[pageNumber]) {
			this.setupCanvas(canvas, this.pages[pageNumber]);
		} else {
			$.ajax({
				beforeSend: function() {/* Add throbber */ },
				complete: function() {/* Remove throbber */ },
				error: this.onAjaxError,
				global: false,
				success: function(data) {
					if (data.imageData && data.pageNumber) {
						var bg = new Image();
						bg.src = data.imageData;
						
						Drawing.pages[data.pageNumber] = {
							lines: new Array(),
							background: bg,
							pageNumber: data.pageNumber,
							sourceHeight: data.sourceHeight,
							sourceWidth: data.sourceWidth
						};
						
						Drawing.setupCanvas(canvas, Drawing.pages[data.pageNumber]);
					}
				},
				type: 'GET',
				url: '/document_vault/document/image/' + documentId + '/' + pageNumber
			});
		}
	},

	print: function(printerId, documentId) {
		$.ajax({
			beforeSend: function() {/* Add throbber */ },
			complete: function() {/* Remove throbber */ },
			error: this.onAjaxError,
			global: false,
			success: function(data) {
				if (data.status == 'success') {
					Drawing._alert('Print has been queue', '<p><span class="ui-icon ui-icon-info" style="float: left; margin: 0 7px 50px 0;"></span>This document has been sent to the printer</p>');
				} else {
					Drawing._alert('There has been an error', '<p><span class="ui-icon ui-icon-alert" style="float: left; margin: 0 7px 50px 0;"></span>This document has NOT been sent to the printer</p>');
				}
			},
			type: 'GET',
			url: '/document_vault/printQueue/push/' + printerId + '/' + documentId
		});
	},
	
	submitPage: function(documentId, pageNumber) {
		var $dialog = $('#dialog-message');
		
		if (pageNumber >= this.pages.length) {
			$('#progressbar').progressbar('value', 100);
			this.finishDocument(documentId);
		} else {
			var page = this.pages[pageNumber];
			
			$('#progressbar').progressbar('value', Math.round(100 * (pageNumber / this.pages.length), 0));
			
			var imageData = this.drawHiddenCanvas(page);
			
			$.ajax({
				beforeSend: function() {/* Add throbber */ },
				complete: function() {/* Remove throbber */ },
				data: {imageData: imageData},
				error: this.onAjaxError,
				global: false,
				success: function(data) {
					Drawing.submitPage(documentId, pageNumber + 1);
				},
				type: 'POST',
				url: '/document_vault/document/sign/' + documentId + '/' + pageNumber
			});
		}
	},
	// !AJAX Stuff
	
	init: function() {
		//window.scrollTo(0, 60);
		this.$main = $('#main');
		this.can = document.getElementById('can');
		this.previousPoint = this.ORIGIN;
		this.pages = new Array(parseInt($('#pageCount').val() || 1));
		
		this.can.ontouchstart = function(e) {
			if (Drawing.trackingTouchId == null) {
				Drawing.trackingTouchId = e.touches[0].identifier;
				
				Drawing.previousPoint = Drawing.convertEventToPoint(e.touches[0]);
			}
		};
		
		this.can.ontouchmove = function(e) {
			var currentTouch = null;
			for (var i = 0; i < e.touches.length; i++) {
				if (Drawing.trackingTouchId == e.touches[i].identifier) {
					currentTouch = e.touches[i];
				}
			}
			
			if (currentTouch) {
				Drawing.doMove(currentTouch);
			}
		};
		
		this.can.ontouchend = function(e) {
			var currentTouch = null;
			for (var i = 0; i < e.changedTouches.length; i++) {
				if (Drawing.trackingTouchId == e.changedTouches[i].identifier) {
					currentTouch = e.changedTouches[i];
				}
			}
			
			if (currentTouch) {
				Drawing.trackingTouchId = null;
				Drawing.doEnd(currentTouch);
			}
		};
		
		// Do this server side, maybe
		if (!navigator.userAgent.match(/iPhone|iPad/i)) {
			$('#can').mousedown(function(e) {
				if (e.which == 1) {
					Drawing.isPainting = true;
					Drawing.previousPoint = {x: e.pageX, y: e.pageY};
				}
			});
			
			$('#can').mousemove(function(e) {
				if (Drawing.isPainting) {
					Drawing.doMove(e);
				}
			});
			
			$('#can').mouseup(function(e) {
				if (Drawing.isPainting) {
					Drawing.doEnd(e);
					Drawing.isPainting = false;
				}
			});
			
			$('#can').mouseleave(function(e) {
				if (Drawing.isPainting) {
					Drawing.doEnd(e);
					Drawing.isPainting = false;
				}
			});
		}
		
		$('#clearcan').click(function() {
			Drawing.currentPage.lines.splice(0, Drawing.currentPage.lines.length);
			Drawing.draw(Drawing.can, Drawing.currentPage);
		});
		
		$('#undo').click(function() {
			var splicePoint = Drawing.currentPage.lines.length;
			for (var i = Drawing.currentPage.lines.length - 2; i >= 0; i--) {
				if (Drawing.currentPage.lines[i] == Drawing.LINEBREAK || i == 0) {
					splicePoint = i;
					break;
				}
			}
			Drawing.currentPage.lines.splice(splicePoint, Drawing.currentPage.lines.length);
			Drawing.addBreak(Drawing.currentPage);
			Drawing.draw(Drawing.can, Drawing.currentPage);
		});
		
		$('#save').click(function() {
			$('#confirm-submit').dialog('open');
		});
		
		$('#close').click(function() {
			window.location.href = '/document_vault'
		});
		
		$(window).hashchange(function() {
			var newPage = parseInt(location.hash.substring(1)) || 0;
			
			if (!Drawing.currentPage || newPage != Drawing.currentPage.pageNumber) {
				Drawing.getPage(Drawing.can, $('#documentId').val(), newPage);
			}
		});
		
		$('.arrow a').click(function() {
			if ($(this).is('.disabled')) {
				return false;
			}
		});
		
		window.onorientationchange = function() {
			if (!Drawing.isZoomedIn) {
				$('#viewAll').click();
			}
		};
		
		$('#viewAll').click(function() {
			Drawing.viewArea(Drawing.can, Drawing.currentPage, Drawing.ORIGIN, {x:Drawing.currentPage.background.width, y:Drawing.currentPage.background.height}, 'width');
			Drawing.isZoomedIn = false;
		});
		
		$('#pen').click(function(e) {
			$(this).toggleClass('on');
			
			if ($(this).is('.on')) {
				Drawing.$main.css('cursor', 'crosshair');
			} else {
				Drawing.$main.css('cursor', 'default');
			}
		});
		
		$('#dialog-message').dialog({
			autoOpen: false,
			buttons: {},
			closeOnEscape: false,
			draggable: false,
			modal: true,
			open: function(event, ui) {
				$('#progressbar').progressbar('value', 0); 
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
					Drawing.submitPage($('#documentId').val(), 0);
				},
				'Cancel' :function() {
					$(this).dialog('close');
				}
			},
			draggable: false,
			modal: true,
			resizable: false
		});
		
		$('#progressbar').progressbar({
			change: function() {
				$('#pblabel').text(Math.min(100, $(this).progressbar('option', 'value')) + '%');
			},
			value: 1
		});
		
		$('#printer-select').dialog({
			autoOpen: false,
			buttons: {
				'Print': function() {
					$(this).dialog('close');
					Drawing.print($('#printer').val(), $('#documentId').val());
				},
				'Cancel' :function() {
					$(this).dialog('close');
				}
			},
			closeOnEscape: false,
			draggable: false,
			modal: true,
			open: function(event, ui) {	},
			resizable: false,
			width: 400
		});

		$('#print').click(function() {
			$('#printer-select').dialog('open');
		});
		
		// Load the page indicated by the location hash
		$(window).hashchange();
	}
}

$(document).ready(function() {
	Drawing.init();
});
