<span id="saved"><img src="${resource(dir:'images', file:'dialog-yes.png')}" alt="" />${message}
<jq:jquery>
	setTimeout(function() {$('#saved').fadeOut('fast', function() {$(this).remove()})}, 2000);
</jq:jquery>
</span>
