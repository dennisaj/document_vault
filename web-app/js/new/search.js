/** [Document Vault] Search **/ 
var DocumentSearch = {
	settingHash: false,

	setHash: function(hash) {
		var self = this;
		this.settingHash = true;
		try {
			location.hash = encodeURI(hash);
		} finally {
			// Short timeout to give the event handler time to fire
			setTimeout(function() { self.settingHash = false; }, 10);
		}
	},

	init: function() {
		var self = this;

		$(window).hashchange(function(event) {
			if (self.settingHash) {
				return;
			}

			$('#q').val(decodeURI(location.hash.substring(1)));
			$('#searchForm').submit();
		});

		$('#reset1').click(function() {
			$('#q').val('');
			$('#searchForm').submit();
		});

		if (location.hash) {
			$(window).hashchange();
		}

		$('#q').focus();
	}
};
