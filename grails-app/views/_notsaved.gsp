<span id="notsaved"><img src="${resource(dir:'images', file:'dialog-error.png')}" alt="" />${message}
<jq:jquery>
	setTimeout(function() {$('#notsaved').fadeOut('fast', function() {$(this).remove()})}, 10000);
</jq:jquery>
</span>
