var DocumentNote = {
	urls: {},

	show: function(id, button) {
		$(button).toggleClass('ui-state-active');
		$(id).toggleClass('hidden');

		return false;
	}
};
