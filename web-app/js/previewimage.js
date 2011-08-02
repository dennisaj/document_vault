
var PreviewImage = {
	show: function(uri) {
		$('body').append('<div id="preview"></div>');
		var $imageDialog = $('#preview');

		var $clone = $('#spinner').clone().attr('id', 'spinner-clone').css('display', 'block').width(600).height(600);
		$imageDialog.append($clone);

		$imageDialog.dialog({
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

		var image = new Image();
		image.src = uri;

		var load = function() {
			var width = Math.min(600, image.width);
			var scale = width / image.width;
			var height = scale * image.height;

			$imageDialog.html($(image).width(width).height(height));

			// Tiny timeout before re-centering to help out Chrome.
			setTimeout(function() {$imageDialog.dialog('option', 'position', 'center')}, 2);
		};

		if (image.complete) {
			load();
		} else {
			image.onload = load;
		}
	}
};
