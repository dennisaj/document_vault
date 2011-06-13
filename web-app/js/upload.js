var Upload = {
	fileCount: 0,
	urls: {},
	init: function(urls) {
		var self = this;
		this.urls = urls;

		$('#fileupload').fileupload({
			previewFileTypes: /^image\/(gif|jpeg|png|bmp)$/,
			url: this.urls.upload
		}).bind('fileuploadadd', function(e, data) {
			self.fileCount += data.files.length;
			$('button', '#action-buttons').button('enable');
		}).bind('fileuploaddone fileuploadfail fileuploadstop', function (e, data) {
			if (!data.files) {
				return;
			}

			self.fileCount -= data.files.length;
			if (self.fileCount <= 0) {
				self.fileCount = 0;
				$('button', '#action-buttons').button('disable');
			}
		});

		$('button', '#action-buttons').button('disable');
	}
};
