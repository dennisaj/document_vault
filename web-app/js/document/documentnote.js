var DocumentNote = {
	urls: {},

	show: function(id, button) {
		$(button).toggleClass('ui-state-highlight', $(id).toggleClass("hidden").is(":not(.hidden)"));
		return false;
	}
};
