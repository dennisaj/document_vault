"use strict";
// Replaces {0}, {1}, ... {n} in a String with the respective argument passed to the function
String.prototype.format = function () {
	var args = arguments;
	return this.replace(/\{(\d+)\}/g, function (m, n) { return args[n]; });
};

function round(number, places) {
	places = places || 1
	return parseFloat(number.toFixed(places));
}

$.extend({
	htmlEncode: function(value) {
		return $('<div/>').text(value).html();
	},
	htmlDecode: function(value){
		return $('<div/>').html(value).text();
	}
});

$.support.touch = (typeof Touch === 'object' || 'ontouchstart' in window);

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
if (!Object.keys)  {
	Object.keys = function(o) {
		if (o !== Object(o)) {
			throw new TypeError('Object.keys called on non-object');
		}

		var ret = [], p;
		for(p in o) {
			if (Object.prototype.hasOwnProperty.call(o, p)) {
				ret.push(p);
			}
		}

		return ret;
	}
}
