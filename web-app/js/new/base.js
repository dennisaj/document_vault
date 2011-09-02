/** [Document Vault] Base **/ 
jQuery(function($) {
	// Dummy up additional ui hints for global search that cannot be done with css transitions
	$("#q, #tagq").bind("focus blur", function(event) {
		$(event.target).parent("div").toggleClass("active", (event.type === "focus"));
	});
});
