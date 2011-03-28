//global objects
var can,
currentHeight = 0,
currentWidth = 0,
currentPage,
LINEBREAK = 'LINEBREAK',
$main,
isDragging = false,
isPainting = false,
isZoomedIn = false,
minVisible = 150,
ORIGIN = {x:0, y:0},
previousPoint,
pages,
scale = 1,
scrollCanX = 0,
scrollCanY = 0,
trackingTouchId = null,
ZOOM_SCALE = .3;

function addBreak(page) {
	// Don't add consecutive LINEBREAKs
	if (page.lines[page.lines.length - 1] != LINEBREAK) {
		page.lines.push(LINEBREAK);
	}
}

function addLine(page, line) {
	page.lines.push(line);
}

function block(e) {
	e.preventDefault();
}

// Make sure at least 150 x 150 pixels of the screen are visible at any time. 
function canScroll(x, y) {
	
	if ((currentWidth + x) < minVisible) {
		return false;
	} else if ((currentHeight + y) < minVisible) {
		return false;
	} else if ($main.width() - x < minVisible) {
		return false;
	} else if ($main.height() - y < minVisible) {
		return false;
	}
	
	return true;
}

function clearCanvas(canvas, page) {
	var context = canvas.getContext('2d');
	
	context.clearRect(0, 0, canvas.width, canvas.height);
	canvas.width = canvas.width;
	context.drawImage(page.background, 0, 0, canvas.width, canvas.height);
}

function convertEventToPoint(touch) {
	if (touch) {
		return {x: touch.pageX, y: touch.pageY};
	} else {
		return ORIGIN;
	}
}

function doEnd(event) {
	var isDrawing = $('#pen').is('.on');
		
	if (!isDrawing && !isDragging) {
		if (isZoomedIn) {
			$('#viewAll').click();
		} else {
			var point = scalePoint(convertEventToPoint(event));
			
			var zoomHeight = ZOOM_SCALE * currentPage.background.height;
			var heightOffset = zoomHeight / 2;
			
			var zoomStart = {
				x: point.x,
				y: point.y - heightOffset
			};
			
			var zoomEnd = {
				x: point.x + (currentPage.background.width * ZOOM_SCALE), 
				y: point.y + heightOffset
			};
			
			viewArea(can, currentPage, zoomStart, zoomEnd, 'height');
			
			isZoomedIn = true;
		}
	} else if (isDrawing) {
		addBreak(currentPage);
	} else {
		$main.css('cursor', 'default');
	}
	
	isDragging = false;
}

function doMove(event) {
	isDragging = true;
	var point = convertEventToPoint(event);
	var isDrawing = $('#pen').is('.on');
	
	if (!isDrawing) {
		$main.css('cursor', 'move');
		dragCanvas(previousPoint, point);
	} else {
		var line = {
			start: scalePoint(previousPoint),
			end: scalePoint(point),
		};
		addLine(currentPage, line);
		drawLine(can, line);
	}
	
	previousPoint = point;
}

function dragCanvas(oldPoint, newPoint) {
	var newScrollCanX = scrollCanX + newPoint.x - oldPoint.x;
	var newScrollCanY = scrollCanY + newPoint.y - oldPoint.y;
	
	if (canScroll(newScrollCanX, newScrollCanY)) {
		scrollCanX = newScrollCanX;
		scrollCanY = newScrollCanY;
		can.style.webkitTransform = 'translate(' + scrollCanX + 'px, ' + scrollCanY + 'px)';
		can.style.MozTransform = 'translate(' + scrollCanX + 'px, ' + scrollCanY + 'px)';
	}
}

// Rename me
function draw(canvas, page) {
	clearCanvas(canvas, page);
	
	for (var i = 0; i < page.lines.length; i++) {
		if (page.lines[i] == LINEBREAK) {
			continue;
		}
		drawLine(canvas, page.lines[i]);
	}
}

/**
 * 
 * @param page
 * @returns a base64 encoded png or "data:image/png;base64," if page is null.
 */
function drawHiddenCanvas(page) {
	if (!page) {
		return "data:image/png;base64,";
	}
	
	var hiddenCanvas = document.getElementById('hidden-canvas');
	hiddenCanvas.width = page.background.width;
	hiddenCanvas.height = page.background.height;
	
	var hiddenContext = hiddenCanvas.getContext('2d');
	
	hiddenContext.clearRect(0, 0, hiddenCanvas.width, hiddenCanvas.height);
	hiddenCanvas.width = hiddenCanvas.width;
	
	for (var i = 0; i < page.lines.length; i++) {
		if (page.lines[i] == LINEBREAK) {
			continue;
		}
		drawLine(hiddenCanvas, page.lines[i]);
	}
	
	return hiddenCanvas.toDataURL();
}

function drawLine(canvas, line) {
	var context = canvas.getContext('2d');
	
	context.strokeStyle = 'rgba(0, 128, 0, 1)';
	context.lineJoin = 'round';
	context.lineWidth = 1;
	context.beginPath();
	context.moveTo(line.start.x, line.start.y);
	context.lineTo(line.end.x, line.end.y);
	context.closePath();
	context.stroke();
}

function realSetupCanvas(canvas, page) {
	currentPage = page;
			
	// If the page changed to the page we requested, update the arrows
	//if (currentPage.pageNumber == newPage) {
		$('#right-arrow a').attr('href', '#' + Math.min(pages.length - 1, currentPage.pageNumber + 1));
		$('#left-arrow a').attr('href', '#' + Math.max(0, currentPage.pageNumber - 1));
		
		$('.arrow a').removeClass('disabled');
		
		if (currentPage.pageNumber == 0) {
			$('#left-arrow a').addClass('disabled');
		}
		
		if (currentPage.pageNumber == pages.length - 1) {
			$('#right-arrow a').addClass('disabled');
		}
	//}
	
	
	canvas.width = page.background.width;
	canvas.height = page.background.height;
	
	viewArea(canvas, page, ORIGIN, {x:page.background.width, y:page.background.height}, 'width');
	draw(canvas, page);
}

function scalePoint(point) {
	return {
		x: (point.x - scrollCanX) / scale, 
		y: (point.y - scrollCanY) / scale
	}
}

function setupCanvas(canvas, page) {
	if (page.background.complete) {
		realSetupCanvas(canvas, page)
	} else {
		page.background.onload = function() {
			realSetupCanvas(canvas, page)
		};
	}
}

// Given opposite corners of a rectangle, zoom the screen to that area.
function viewArea(canvas, page, point1, point2, scaleBy) {
	var upperLeftCorner = {
		x: Math.min(point1.x, point2.x), 
		y: Math.min(point1.y, point2.y)
	};
	
	var lowerRightCorner = {
		x: Math.max(point1.x, point2.x), 
		y: Math.max(point1.y, point2.y)
	};
	
	
	var newWidth = Math.max(page.background.width * .10, lowerRightCorner.x - upperLeftCorner.x);
	var newHeight = Math.max(page.background.height * .10, lowerRightCorner.y - upperLeftCorner.y);
	
	if (scaleBy == 'width') {
		scale = $main.width() / newWidth;
	} else {
		scale = $main.height() / newHeight;
	}
	
	scrollCanX = -upperLeftCorner.x * scale;
	scrollCanY = -upperLeftCorner.y * scale;
	currentWidth = page.background.width * scale;
	currentHeight = page.background.height * scale;
	canvas.style.width = currentWidth + 'px';
	canvas.style.height = currentHeight + 'px';

	canvas.style.webkitTransform = 'translate(' + scrollCanX + 'px, ' + scrollCanY + 'px)';
	canvas.style.MozTransform = 'translate(' + scrollCanX + 'px, ' + scrollCanY + 'px)';
}

function onAjaxError(jqXHR, textStatus, errorThrown) {
	$('#dialog-message').dialog('close');

	_alert('Unable to save the signatures', '<p><span class="ui-icon ui-icon-alert" style="float: left; margin: 0 7px 50px 0;"></span>Oopsie! ' + textStatus + '</p>');
}

function _alert(title, html) {
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
}

// AJAX stuff
function finishDocument(documentId) {
	$dialog = $('#dialog-message');
	$.ajax({
		beforeSend: function() {/* Add throbber */ },
		complete: function() {/* Remove throbber */ },
		error: onAjaxError,
		global: false,
		success: function(data) {
			$dialog.dialog('close');

			_alert('Signing complete', '<p><span class="ui-icon ui-icon-circle-check" style="float: left; margin: 0 7px 50px 0;"></span>The document has been successfully signed.</p>');
		},
		type: 'GET',
		url: '/document_vault/document/finish/' + documentId
	});
}

function getPage(canvas, documentId, pageNumber) {
	if (pageNumber >= pages.length) {
		pageNumber = pages.length - 1;
	} else if (pageNumber < 0) {
		pageNumber = 0;
	}
	
	if (pages[pageNumber]) {
		setupCanvas(canvas, pages[pageNumber]);
	} else {
		$.ajax({
			beforeSend: function() {/* Add throbber */ },
			complete: function() {/* Remove throbber */ },
			error: onAjaxError,
			global: false,
			success: function(data) {
				if (data.imageData && data.pageNumber) {
					var bg = new Image();
					bg.src = data.imageData;
					
					pages[data.pageNumber] = {
						lines: new Array(),
						background: bg,
						pageNumber: pageNumber
					};
					
					setupCanvas(canvas, pages[data.pageNumber]);
				}
			},
			type: 'GET',
			url: '/document_vault/document/image/' + documentId + '/' + pageNumber
		});
	}
}

function submitPage(documentId, pageNumber) {
	$dialog = $('#dialog-message');
	
	if (pageNumber >= pages.length) {
		$('#progressbar').progressbar('value', 100);
		finishDocument(documentId);
	} else {
		var page = pages[pageNumber];
		
		$('#progressbar').progressbar('value', 100 * (pageNumber / pages.length));
		
		var imageData = drawHiddenCanvas(page);
		
		$.ajax({
			beforeSend: function() {/* Add throbber */ },
			complete: function() {/* Remove throbber */ },
			data: {imageData: imageData},
			error: onAjaxError,
			global: false,
			success: function(data) {
				submitPage(documentId, pageNumber + 1);
			},
			type: 'POST',
			url: '/document_vault/document/sign/' + documentId + '/' + pageNumber
		});
	}
}
// !AJAX Stuff

$(document).ready(function() {
	//window.scrollTo(0, 60);
	$main = $('#main');
	can = document.getElementById('can');
	previousPoint = ORIGIN;
	pages = new Array(parseInt($('#pageCount').val() || 1));
	
	can.ontouchstart = function(e) {
		if (trackingTouchId == null) {
			trackingTouchId = e.touches[0].identifier;
			
			previousPoint = convertEventToPoint(e.touches[0]);
		}
	};
	
	can.ontouchmove = function(e) {
		var currentTouch = null;
		for (var i = 0; i < e.touches.length; i++) {
			if (trackingTouchId == e.touches[i].identifier) {
				currentTouch = e.touches[i];
			}
		}
		
		if (currentTouch) {
			doMove(currentTouch);
		}
	};
	
	can.ontouchend = function(e) {
		var currentTouch = null;
		for (var i = 0; i < e.changedTouches.length; i++) {
			if (trackingTouchId == e.changedTouches[i].identifier) {
				currentTouch = e.changedTouches[i];
			}
		}
		
		if (currentTouch) {
			trackingTouchId = null;
			doEnd(currentTouch);
		}
	};
	
	// Do this server side, maybe
	if (!navigator.userAgent.match(/iPhone|iPad/i)) {
		$('#can').mousedown(function(e) {
			if (e.which == 1) {
				isPainting = true;
				previousPoint = {x: e.pageX, y: e.pageY};
			}
		});
		
		$('#can').mousemove(function(e) {
			if (isPainting) {
				doMove(e);
			}
		});
		
		$('#can').mouseup(function(e) {
			if (isPainting) {
				doEnd(e);
				isPainting = false;
			}
		});
		
		$('#can').mouseleave(function(e) {
			if (isPainting) {
				doEnd(e);
				isPainting = false;
			}
		});
	}
	
	$('#clearcan').click(function() {
		currentPage.lines.splice(0, currentPage.lines.length);
		draw(can, currentPage);
	});
	
	$('#undo').click(function() {
		splicePoint = currentPage.lines.length;
		for (var i = currentPage.lines.length - 2; i >= 0; i--) {
			if (currentPage.lines[i] == LINEBREAK || i == 0) {
				splicePoint = i;
				break;
			}
		}
		currentPage.lines.splice(splicePoint, currentPage.lines.length);
		addBreak(currentPage);
		draw(can, currentPage);
	});
	
	$('#save').click(function() {
		$('#confirm-submit').dialog('open');
		//window.open(drawHiddenCanvas(currentPage));
	});
	
	$(window).hashchange(function() {
		var newPage = parseInt(location.hash.substring(1)) || 0;
		
		if (!currentPage || newPage != currentPage.pageNumber) {
			getPage(can, $('#documentId').val(), newPage);
		}
	});
	
	$('.arrow a').click(function() {
		if ($(this).is('.disabled')) {
			return false;
		}
	});
	
	window.onorientationchange = function() {
		if (!isZoomedIn) {
			$('#viewAll').click();
		}
	};
	
	$('#viewAll').click(function() {
		viewArea(can, currentPage, ORIGIN, {x:currentPage.background.width, y:currentPage.background.height}, 'width');
		isZoomedIn = false;
	});
	
	$('#pen').click(function(e) {
		$(this).toggleClass('on');
		
		if ($(this).is('.on')) {
			$main.css('cursor', 'crosshair');
		} else {
			$main.css('cursor', 'default');
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
				submitPage($('#documentId').val(), 0);
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
	
	// Load the page indicated by the location hash
	$(window).hashchange();
});
