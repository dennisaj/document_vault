var Upload = {
	urls: {},
	init: function(urls) {
		this.urls = urls;

		$('#fileupload').fileupload({
			previewFileTypes: /^image\/(gif|jpeg|png|bmp)$/,
			url: this.urls.upload
		});

		$('#fileupload .files a:not([target^=_blank])').live('click', function (e) {
			e.preventDefault();
			$('<iframe style="display:none;"></iframe>').prop('src', this.href).appendTo('body');
		});
	}
};
