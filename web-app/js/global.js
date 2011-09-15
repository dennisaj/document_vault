"use strict";
// Replaces {0}, {1}, ... {n} in a String with the respective argument passed to the function
String.prototype.format = function () {
	var args = arguments;
	return this.replace(/\{(\d+)\}/g, function (m, n) { return args[n]; });
};

function round(number, places) {
	places = places || 1;
	return parseFloat(number.toFixed(places));
}

$.extend({
	htmlEncode: function (value) {
		return $('<div/>').text(value).html();
	},
	htmlDecode: function (value) {
		return $('<div/>').html(value).text();
	}
});

// Attach global handler for ajax start and stop events to show the spinner to denote background activity to user
// We delay showing the spinner by 1s to avoid "bouncy" notifications, and the call to stop before the delay will clear
// the animation queue, thus preventing the show animation from happening if the handler is called a second time from ajaxStop
// 200ms seems a reasonable delay - tests show responses between 50ms and 150ms (again, locally). Adjust the delay for production
function Spinner() {
	var spcontainer = $("#spinner"),
		spmessage = $("#spinner-message"),
		that = this;
	
	this.showdelay = 200;
	
	this.message = function(msg) {
		if (msg !== undefined && typeof msg === "string") {
			return spmessage.html(msg);
		}
		return;
	};
	
	this.toggle = function(dly, tp, msg) {
		that.message(msg);
		return spcontainer.stop(true).delay(dly).animate({ top: tp }, 220);
	};
	
	this.show = function(msg) { return that.toggle(that.showdelay, -2, msg); };
	this.hide = function() { return that.toggle(0, -50); };
	
	spcontainer
		.ajaxStart(that.show)
		.ajaxStop(that.hide);
};

// Extend jQuery with an instance of Spinner
$.extend({ spinner: new Spinner() });

$.support.touch = (typeof Touch === 'object' || window.ontouchstart === undefined);

// Because IE sucks
// See http://stackoverflow.com/questions/1695376/msie-and-addeventlistener-problem-in-javascript
function bindEvent(element, eventName, eventHandler) {
	if (element.addEventListener) {
		element.addEventListener(eventName, eventHandler, false);
	} else if (element.attachEvent) {
		element.attachEvent('on' + eventName, eventHandler);
	}
}

// Compatibility for pre-Firefox 4 and pre-IE 9
// https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/Object/keys
// 53: //if (typeof o !== Object(o)) {
if (!Object.keys) {
	Object.keys = function (o) {
		if (typeof o === "object" && !!o) {
			throw new TypeError('Object.keys called on non-object');
		}

		var ret = [], p;
		for (p in o) {
			if (Object.prototype.hasOwnProperty.call(o, p)) {
				ret.push(p);
			}
		}

		return ret;
	};
}
