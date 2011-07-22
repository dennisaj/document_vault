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
