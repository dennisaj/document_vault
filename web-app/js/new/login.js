/** [Document Vault] Login **/ 
jQuery(function($) {
	var dfd = $.Deferred(), si = $("#signin"), sd = $(".error").hide().slideDown(300);

	dfd
	.done(function() { sd.slideUp(40); })
	.done(function() { si.unbind("submit.dvlogin").submit(); });

	si.bind("submit.dvlogin", function(event) { event.preventDefault(); dfd.resolve(); });
});
