/** [Document Vault] Upload **/ 
var Upload = {
	urls: {},

	init: function(urls) {
		var self = this;
		this.urls = urls;

		$("#fileupload").fileupload({
			autoUpload: true,
			previewFileTypes: /^image\/(gif|jpeg|png|bmp)$/,
			url: self.urls.controller
		}).bind("fileuploaddone fileuploadfail fileuploadstop", function(event, data) {
			if (event.type === "fileuploaddone") {
				//console.log(event, data.context);	
			}
		}).bind("fileuploadprogress", function(event, data) {
			if (data.context) {
				var loaded = parseInt(data.loaded / data.total * 100, 10) + "%", rp = data.context.find(".real-progress");
				rp.find(".progress-ui-value").css({ height: loaded });
				rp.find("span").text(loaded);
			}
		});
	}
};
