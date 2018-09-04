$.fn.popoverMsg = function(msg, type, position, timeLast) {
	var $this = $(this);
	var oldContent = null;
	
	if (typeof type === 'undefined') {
		var type = 'info';
	}
	if (typeof position === 'undefined') {
		var position = 'top';
	}
	if (typeof timeLast === 'undefined') {
		var timeLast = 2000;
	}
	if (typeof $this.data('bs.popover') !== 'undefined') {
		oldContent = $this.data('bs.popover').options.content;
		$this.data('bs.popover').options.content = '<div class="alert alert-' + type + ' class_div_button_popup">' + msg + '</div>';
	} else {
		$this.popover({
			placement: position,
	        html: 'true',
	        trigger : 'manual',
	        content : '<div class="alert alert-' + type + ' class_div_button_popup">' + msg + '</div>'
		});
	}
	setTimeout(function () {
		$this.popover('show');
    }, 500);
	setTimeout(function () {
		if (oldContent) {
	        $this.popover('hide');
			$this.data('bs.popover').options.content = oldContent;
		} else {
	        $this.popover('destroy');			
		}
    }, timeLast);
}

function getParam(sParam) {
	var sPageURL = window.location.search.substring(1);
	var sURLVariables = sPageURL.split('&');
	for (var i = 0; i < sURLVariables.length; i++)
	{
		var sParameterName = sURLVariables[i].split('=');
		if (sParameterName[0] == sParam)
		{
			return sParameterName[1];
		}
	}
	return null;
}

