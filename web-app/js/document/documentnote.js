var DocumentNote = {
	urls: {},

	show: function(id, button) {
		var self = this;

		$(button).toggleClass('ui-state-active');
		$(id).toggleClass('hidden');
	}
};
