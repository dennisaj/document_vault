var Upload = {
	urls: {},
	init: function(urls) {
		this.urls = urls;

		$('#fileupload').fileupload({
			previewFileTypes: /^image\/(gif|jpeg|png|bmp)$/,
			url: this.urls.upload
		});
	}
};
