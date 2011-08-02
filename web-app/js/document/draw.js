var Draw = {
	highlightOpacity: .7,
	LINEBREAK: 'LB',
	lowlightOpacity: .2,

	addBreak: function(page) {
		// Don't add consecutive LINEBREAKs
		if (page.lines[page.lines.length - 1] != this.LINEBREAK) {
			page.lines.push(this.LINEBREAK);
		}
	},

	addLine: function(page, line) {
		page.lines.push(line);
	},

	addHighlight: function(page, party, corners) {
		page.unsavedHighlights[party] = page.unsavedHighlights[party] || [];
		page.unsavedHighlights[party].push(corners);
	},

	clearCanvas: function(canvas, page) {
		var context = canvas.getContext('2d');

		context.clearRect(0, 0, canvas.width, canvas.height);
		canvas.width = canvas.width;
		if (page.background.src) {
			context.drawImage(page.background, 0, 0, canvas.width, canvas.height);
		}
	},

	// Rename me
	draw: function(canvas, page) {
		var self = this;
		this.clearCanvas(canvas, page);

		for (var i = 0; i < page.lines.length; i++) {
			if (page.lines[i] == this.LINEBREAK) {
				continue;
			}
			this.drawLine(canvas, page.lines[i]);
		}

		// TODO: Remove the party.js dependency
		// Only draw the highlights when we are requesting signatures
		if (Party.isRequestingSignatures()) {
			var activePartyId = Party.getSelectedPartyRow().attr('id');

			// Merge saved and unsaved highlights.
			var highlights = $.extend(true, {}, page.savedHighlights);
			$.each(page.unsavedHighlights, function(key, value) {
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
		} else {
			if (page.unsavedHighlights) {
			// Print sign-box highlights if we are not in highlighting mode.
				$.each(page.unsavedHighlights[SignBox.partyName] || [], function(key, value) {
					self.highlight(canvas, page, value, SignBox.partyColor, this.highlightOpacity);
				});
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

	highlight: function(canvas, page, corners, color, opacity) {
		// If the color isn't set, the highlight will be invisible
		color = color || 'rgba(255, 255, 255, 0)';
		opacity = opacity || this.highlightOpacity;

		var context = canvas.getContext('2d');

		context.fillStyle = color;
		context.globalAlpha = opacity;
		context.fillRect(corners.left, corners.top, corners.width, corners.height);
	},

	isPointInsideBox: function(point, box) {
		if (box.left > point.x) {
			return false;
		} else if (box.top > point.y) {
			return false;
		} else if ((box.left + box.width) < point.x) {
			return false;
		} else if ((box.top + box.height) < point.y) {
			return false;
		}

		return true;
	},

	isPointInsideAnyBox: function(point, boxes) {
		if (!boxes) {
			return;
		}

		var self = this;
		var targetbox = null;

		$.each(boxes, function(key, boxes) {
			if (self.isPointInsideBox(point, boxes)) {
				targetbox = boxes;

				// return false to break out of $.each
				return false;
			}
		});

		return targetbox;
	},

	normalizeCorners: function(point1, point2) {
		return {
			left: Math.min(point1.x, point2.x),
			top: Math.min(point1.y, point2.y),
			width: Math.abs(point1.x - point2.x),
			height: Math.abs(point1.y - point2.y)
		}
	},

	scaleLines: function(page) {
		if (!page || !page.lines || !page.lines.length) {
			return {};
		}

		var scaleX = page.sourceWidth / page.background.width;
		var scaleY = page.sourceHeight / page.background.height;
		var lines = [];

		for (var i = 0; i < page.lines.length; i++) {
			if (page.lines[i] == Draw.LINEBREAK) {
				lines[i] = Draw.LINEBREAK;
				continue;
			}

			lines[i] = {
				a: {
					x: round(page.lines[i].start.x * scaleX),
					y: round(page.lines[i].start.y * scaleY)
				},
				b: {
					x: round(page.lines[i].end.x * scaleX),
					y: round(page.lines[i].end.y * scaleY)
				}
			};
		}

		return lines;
	},

	scalePoint: function(page, point, offset) {
		offset = offset || {left: page.scrollCanX, top: page.scrollCanY}
		return {
			x: (point.x - offset.left) / page.scale,
			y: (point.y - offset.top) / page.scale
		}
	},

	undo: function(canvas, page) {
		var splicePoint = page.lines.length;
		for (var i = page.lines.length - 2; i >= 0; i--) {
			if (page.lines[i] == this.LINEBREAK || i == 0) {
				splicePoint = i;
				break;
			}
		}

		page.lines.splice(splicePoint, page.lines.length);
		this.addBreak(page);
		this.draw(canvas, page);
	},
};
