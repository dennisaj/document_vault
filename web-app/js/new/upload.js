/** [Document Vault] Upload **/ 
jQuery(function($){
	
	$("#fileupload").fileupload({
		autoUpload: true,
		previewFileTypes: /^image\/(gif|jpeg|png|bmp)$/,
		url: controller
	}).bind("fileuploaddone fileuploadfail fileuploadstop", function(event, data) {
		if (event.type === "fileuploaddone") {
			console.log(event, data.context);	
		}
		
	});
	
});