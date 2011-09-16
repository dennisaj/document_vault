/** [Document Vault] Base **/ 
jQuery(function($) {
	// Dummy up additional ui hints for global search that cannot be done with css transitions
	$("#q, #tagq").bind("focus blur", function(event) {
		$(event.target).parent("div").toggleClass("active", (event.type === "focus"));
	});

	$('.menu > ul > li > a').bind('click', function(event) {
		event.stopPropagation();
		var $this = $(this);
		if ($this.parent().is('.active')){
			$this.parent().removeClass('active');
		} else{
			$('.menu ul li.active').removeClass('active');
			$this.parent().addClass('active');
		}
		return false;
	});

	$(document).bind('click', function(event) {
		if (!$(event.target).parents('.menu').length) {
			$('.menu > ul > li.active').removeClass('active');
		}
	});
});
