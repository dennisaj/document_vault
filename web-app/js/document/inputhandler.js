var InputHandler = {
	handlingInput: null,
	isMouseDown: false,
	isMoving: false,
	mtouch: false,
	previousPoint: null,
	trackingTouchId: null,

	init: function(_handlingInput, $target) {
		var self = this;
		this.previousPoint = {x:0, y:0};
		this.handlingInput = _handlingInput;

		if ($.support.touch) {
			$target.bind('touchstart', function(event) {
				var e = event.originalEvent;
				self.mtouch = (e.targetTouches.length > 1);

				if (self.trackingTouchId == null) {
					var touch = e.targetTouches[0];

					self.trackingTouchId = touch.identifier;
					self.previousPoint = Draw.convertEventToPoint(touch);
					self.handlingInput.doStart(e);
				}
			}).bind('gesturechange', function(event) {
				self.handlingInput.doGestureChange(event);
			}).bind('touchmove', function(event) {
				self.isMoving = true;
				var e = event.originalEvent;
				var currentTouch = null;

				for (var i = 0; i < e.touches.length; i++) {
					if (self.trackingTouchId == e.touches[i].identifier) {
						currentTouch = e.touches[i];
					}
				}

				if (currentTouch) {
					var touchEvent = $.extend({}, e);
					touchEvent.pageX = currentTouch.pageX;
					touchEvent.pageY = currentTouch.pageY;
					self.handlingInput.doMove(touchEvent);
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
					var touchEvent = $.extend({}, e);
					touchEvent.pageX = currentTouch.pageX;
					touchEvent.pageY = currentTouch.pageY;
					self.handlingInput.doEnd(touchEvent);
				}

				// We are still in multitouch mode if there is more than one touch and the tracked touch wasn't ended.
				self.mtouch = (e.targetTouches.length > 1) && !currentTouch;
				self.isMoving = false;
			});
		} else {
			$target.bind('mousedown', function(e) {
				if (e.which == 1) {
					self.isMouseDown = true;
					self.previousPoint = Draw.convertEventToPoint(e);
					self.handlingInput.doStart(e);
				}
			}).bind('mousemove', function(e) {
				if (self.isMouseDown) {
					self.isMoving = true;
					self.handlingInput.doMove(e);
				}
			}).bind('mouseup', function(e) {
				if (self.isMouseDown) {
					self.handlingInput.doEnd(e);
					self.isMouseDown = false;
					self.isMoving = false;
				}
			}).bind('mouseleave', function(e) {
				if (e.toElement && e.toElement.id != 'box' && self.isMouseDown) {
					self.handlingInput.doEnd(e);
					self.isMouseDown = false;
					self.isMoving = false;
				}
			});
		}
	}
};
