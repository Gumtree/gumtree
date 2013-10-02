
var refresh = function(){
	var imgUrl = histmemUrl + "&timestamp=" + new Date().getTime();
	try{
		$.get(imgUrl, function(data,status){
			if (status == "success") {
				$("#histmemImage").attr("src", imgUrl);
			} 
		});
	} catch (e) {
	}
};

jQuery(document).ready(function(){
	$(document).attr("title", title + " - Histogram Snapshot");
	$('#titleString').text(title);
	$("#deviceList").append('<li class="ui-li ui-li-static ui-btn-up-c"><img id="histmemImage" src="' + histmemUrl + '" alt="Loading error. Please refresh again."></li>');
	
	$("#getDom").click(function() {
		refresh();
	});

	$("#statusMainButton").click(function() {
		window.location = "mobile.html";
	})
});

