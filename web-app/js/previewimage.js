var PreviewImage = {
	show: function(uri) {
		$('body').append('<div id="preview"></div>');
		var $imageDialog = $('#preview');
		var image = new Image();
		image.src = uri;

		var load = function() {
			$imageDialog.append($(image).width(Math.min(600, image.width)));

			$imageDialog.dialog({
				autoOpen: false,
				close: function(event, ui) {
					$imageDialog.remove();
				},
				modal: true,
				open: function(event, ui) {
					$('.ui-widget-overlay').click(function() {
						$imageDialog.dialog('close');
					});
				},
				resizable: false,
				width: 'auto'
			});

			// Chrome won't center the dialog correctly without this tiny waiting period
			setTimeout(function() {$imageDialog.dialog('open')}, 1);
		};

		if (image.complete) {
			load();
		} else {
			image.onload = load;
		}
	}
};
