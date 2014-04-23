var isMobileBrowser = false;
try {
	(function(a,b){if(/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i.test(a)||/1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0,4)))isMobileBrowser=true})(navigator.userAgent||navigator.vendor||window.opera,null);	
} catch (e) {
}

var title = "NBI Status";
var ins = [
           {"name":"Echidna", "url":"/echidna/status"},
           {"name":"Wombat", "url":"/wombat/status"},
           {"name":"Kowari", "url":"/kowari/status"},
           {"name":"Quokka", "url":"/quokka/status"},
           {"name":"Taipan", "url":"/taipan/status"},
           {"name":"Pelican", "url":"/pelican/status"},
           {"name":"Kookaburra", "url":"/kookaburra/status"},
           {"name":"Dingo", "url":"/dingo/status"},
           {"name":"Bilby", "url":"/bilby/status"},
           ]
var defaultTimeout = 10;
var refresh = function(dict){
	try{
//		$.ajax({
//			cache: false,
//			type: "HEAD",
//			url: dict.url,
//			timeout: 9000,
//			success: function() {
				$.get(dict.url + "/sics/rest/status", {"timestamp" : new Date().getTime()}, function(data,status){
					if (status == "success") {
//						$("#" + dict.name + "_connection").text("OK");
//						$("#" + dict.name + "_connection").css("color", "green");
						var obj = jQuery.parseJSON(data);
						$("#" + dict.name + "_status").text(obj.status);
						if (obj.status == "EAGER TO EXECUTE") {
							$("#" + dict.name + "_status").css("color", "#00c400");
						} else if (obj.status == "COUNTING" || obj.status == "WAIT" || obj.status == "DRIVING") {
							$("#" + dict.name + "_status").css("color", "#FFA500");
						} else if (obj.status == "PAUSED" || obj.status == "PAUSE") {
							$("#" + dict.name + "_status").css("color", "#0000c4");
						} else  {
							$("#" + dict.name + "_status").text("DISCONNECTED");
							$("#" + dict.name + "_status").css("color", "#c40000");
						}
					} else {
//						$("#" + dict.name + "_connection").text("FAULT");
//						$("#" + dict.name + "_connection").css("color", "red");
						$("#" + dict.name + "_status").text("FAULT");
						$("#" + dict.name + "_status").css("color", "red");
					}
				});
//			},
//			error: function(jqXHR, textStatus, errorThrown) {
////				$("#" + dict.name + "_connection").text("FAULT");
////				$("#" + dict.name + "_connection").css("color", "red");
//				$("#" + dict.name + "_status").text("FAULT");
//				$("#" + dict.name + "_status").css("color", "red");
//			}
//		});
	} catch (e) {
		alert("error");
	}
};

var refreshAll = function() {
	for ( var i = 0; i < ins.length; i++) {
		refresh(ins[i]);
	}
}

var timerObject = {
		interval_id : null
};

jQuery(document).ready(function(){
	$(document).attr("title", title);
	$('#titleString').text(title);
	for (i = 0; i < ins.length; i++) {
		$("#insList").append('<li class="ui-li ui-li-divider ui-bar-d ui-first-child" role="heading" data-role="list-divider"><a class="ui_link" href="' + ins[i].url + '">' + ins[i].name.toUpperCase() + '</a></li>');
//		$("#insList").append('<li class="ui-li ui-li-static ui-btn-up-c"><div class="div-inlist-left">connection: </div> <div class="div-inlist" id="' + ins[i].name + '_connection">--</div></li>');
		$("#insList").append('<li class="ui-li ui-li-static ui-btn-up-c"><div class="div-inlist-left">status: </div> <div class="div-inlist" id="' + ins[i].name + '_status">--</div></li>');
	}

	$("#getDom").click(function() {
		refreshAll();
	});

	
	$("#intervalSlider").val(defaultTimeout);
	
	timerObject.interval_id = setInterval(function(){
		refreshAll();
	}, defaultTimeout * 1000);
	
	$("#intervalSlider").on("slidestop", function( event, ui ) {
		clearInterval(timerObject.interval_id);
		timerObject.interval_id = setInterval(function(){
			refreshAll();
		}, $("#intervalSlider").val() * 1000);
	});

	$("#enableAutoRefresh").on("slidestop", function( event, ui ) {
		var isAutoRefresh = $("#enableAutoRefresh").val() == "on";
		if (isAutoRefresh) {
			timerObject.interval_id = setInterval(function(){
				refreshAll();
			}, $("#intervalSlider").val() * 1000);
			$("#intervalSlider").slider("enable");
		} else {
			clearInterval(timerObject.interval_id);
			$("#intervalSlider").slider("disable");
		}
//		$("#intervalSlider").slider({disabled: !isAutoRefresh});
	});
	
	refreshAll();
});

$(document).on('pagebeforeshow', title, function(){
	alert("pagebeforeshow");
	timerObject.interval_id = setInterval(function() {
		refreshAll();
	}, $("#intervalSlider").val() * 1000);
	alert("pagebeforeshow");
}); 

$(document).on('pagehide', title, function(){   
	clearInterval(timerObject.interval_id);  
});  
