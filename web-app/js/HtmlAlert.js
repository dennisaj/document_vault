var HtmlAlert = {
	_alert: function(title, html) {
		var $alert = $('#alert');

		if ($alert) {
			$alert.dialog('close');
			
			$alert.dialog({
				autoOpen: false,
				buttons: {
					'Ok' : function(){
						$(this).dialog('close');
					}
				},
				closeOnEscape: true,
				hasClose: false,
				modal : true,
				resizable: false,
				title: title
			});
			
			$alert.html(html);
			$alert.dialog('open');
		}
	}
};
