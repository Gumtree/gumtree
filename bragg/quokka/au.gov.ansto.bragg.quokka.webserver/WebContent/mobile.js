
var refresh = function(){
	try{
		$.ajax({
			type: 'HEAD',
			url: "sics/rest",
			success: function() {
				$.get("sics/rest/status",function(data,status){
					if (status == "success") {
						$("#connection").text("OK");
						$("#connection").css("color", "green");
						var obj = jQuery.parseJSON(data);
						$("#sicsServer").text(obj.status);
						if (obj.status == "EAGER TO EXECUTE") {
							$("#sicsServer").css("color", "#00c400");
						} else if (obj.status == "COUNTING" || obj.status == "WAIT" || obj.status == "DRIVING") {
							$("#sicsServer").css("color", "#FFA500");
						} else if (obj.status == "PAUSED") {
							$("#sicsServer").css("color", "#0000c4");
						} else  {
							$("#sicsServer").text("DISCONNECTED");
							$("#sicsServer").css("color", "#c40000");
						}
					} else {
						$("#connection").text("FAULT");
						$("#connection").css("color", "red");
						$("#sicsServer").text("--");
						$("#sicsServer").css("color", "black");
					}
				});
			},
			error: function() {
				$("#connection").text("FAULT");
				$("#connection").css("color", "red");
				$("#sicsServer").text("--");
				$("#sicsServer").css("color", "black");
			}            
		});
	} catch (e) {
		alert("error");
	}
	var dUrl = "";
	var cUrl = "";
	for(var i = 0; i < devices.length; i++) {
		for ( var j = 0; j < devices[i].items.length; j++) {
			if (devices[i].items[j].deviceId.indexOf("/") != -1) {
				cUrl += devices[i].items[j].deviceId;
				cUrl += ",";
			} else {
				dUrl += devices[i].items[j].deviceId;
				dUrl += ",";
			}
		}
	}
	var url = "";
	if (dUrl.length > 0) {
		dUrl = dUrl.substring(0, dUrl.length - 1);
		url += "devices=" + encodeURIComponent(dUrl);
	}
	if (cUrl.length > 0) {
		cUrl = cUrl.substring(0, cUrl.length - 1);
		if (dUrl.length > 0) {
			url += "&";
		}
		url += "components=" + encodeURIComponent(cUrl);
	}
	url = "sics/rest/hdbs?" + url;
	$.get(url,function(data,status){
		if (status == "success") {
			var obj = jQuery.parseJSON(data);
			for ( var i = 0; i < devices.length; i++) {
				for ( var j = 0; j < devices[i].items.length; j++) {
					for (var k = 0; k < obj.hdbs.length; k++){
						if (devices[i].items[j].deviceId == obj.hdbs[k].deviceId){
							try{
								$("#" + devices[i].items[j].classId).text(obj.hdbs[k].value + " " + devices[i].items[j].units);
							} catch (e) {
							}
							break;
						} else {
							if (devices[i].items[j].deviceId == obj.hdbs[k].path){
								try{
									$("#" + devices[i].items[j].classId).text(obj.hdbs[k].value + " " + devices[i].items[j].units);
								} catch (e) {
								}
								break;
							}
						}
					}
				}
			}
		}
	});
	
};

var timerObject = {
		interval_id : null
};

jQuery(document).ready(function(){
	$(document).attr("title", title);
	$('#titleString').text(title);
	for (i = 0; i < devices.length; i++) {
		$("#deviceList").append('<li class="ui-li ui-li-divider ui-bar-d ui-first-child" role="heading" data-role="list-divider">' + devices[i].group + '</li>');
		for ( var j = 0; j < devices[i].items.length; j++) {
			$("#deviceList").append('<li class="ui-li ui-li-static ui-btn-up-c"><div class="div-inlist-left">' + devices[i].items[j].title + ': </div> <div class="div-inlist" id="' + devices[i].items[j].classId + '">--</div></li>');
		}
	}

	$("#getDom").click(function() {
		refresh();
	});

	$("#histmemButton").click(function() {
		window.location = "mobilehistmem.html";
	});
	
	timerObject.interval_id = setInterval(function(){
		refresh();
	}, 10000);
	
	$("#intervalSlider").on("slidestop", function( event, ui ) {
		clearInterval(timerObject.interval_id);
		timerObject.interval_id = setInterval(function(){
			refresh();
		}, $("#intervalSlider").val() * 1000);
	});

	$("#enableAutoRefresh").on("slidestop", function( event, ui ) {
		var isAutoRefresh = $("#enableAutoRefresh").val() == "on";
		if (isAutoRefresh) {
			timerObject.interval_id = setInterval(function(){
				refresh();
			}, $("#intervalSlider").val() * 1000);
			$("#intervalSlider").slider("enable");
		} else {
			clearInterval(timerObject.interval_id);
			$("#intervalSlider").slider("disable");
		}
//		$("#intervalSlider").slider({disabled: !isAutoRefresh});
	});
	
	refresh();
});

$(document).on('pagebeforeshow', title, function(){
	alert("pagebeforeshow");
	timerObject.interval_id = setInterval(function() {
		refresh();
	}, 5000);
	alert("pagebeforeshow");
}); 

$(document).on('pagehide', title, function(){   
	clearInterval(timerObject.interval_id);  
});  
