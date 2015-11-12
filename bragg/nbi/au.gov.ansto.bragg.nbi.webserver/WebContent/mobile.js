var isMobileBrowser = false;
try {
	(function(a,b){if(/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i.test(a)||/1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0,4)))isMobileBrowser=true})(navigator.userAgent||navigator.vendor||window.opera,null);	
} catch (e) {
}

$.support.cors = true;

function formatDate(d) {
	var dd = d.getDate();
	if ( dd < 10 ) dd = '0' + dd;
	var mm = d.getMonth()+1;
	if ( mm < 10 ) mm = '0' + mm;
	var yy = d.getFullYear();
	var HH = d.getHours();
	if (HH < 10) HH = '0' + HH;
	var MM = d.getMinutes();
	if (MM < 10) MM = '0' + MM;
	var ss = d.getSeconds();
	if (ss < 10) ss = '0' + ss;
	return yy + '-' + mm + '-' + dd + 'T' + HH + ':' + MM ;
}

function changeHistmemType() {
	var newHistmemUrl;
	if (typeof histmemTypes !== 'undefined' && histmemTypes.length > 0) {
		newHistmemUrl = histmemUrl.replace('$HISTMEM_TYPE', $('#histmem_type').val());
	} else {
		newHistmemUrl = histmemUrl;
	}
	newHistmemUrl += "&timestamp=" + new Date().getTime()

	try{
		$.get(newHistmemUrl, function(data,status){
			if (status == "success") {
				$("#histmemImage").attr("src", newHistmemUrl);
			} 
		});
	} catch (e) {
	}
}

var lastTimeEstimation = -1.;
var evEnabled = false;
var refresh = function(){
	var url = "ns/rest/hdbs?devices=";
	for (var i = 0; i < nsItems.length; i++) {
		url += nsItems[i].deviceId;
		if (i < nsItems.length - 1) {
			url += ",";
		}
	}
	$.get(url, function(data,status){
		if (status == "success") {
			try {
				var obj = jQuery.parseJSON(data);
				for (var i = 0; i < obj.hdbs.length; i++) {
					for (var j = 0; j < nsItems.length; j++) {
						if (nsItems[j].deviceId == obj.hdbs[i].id) {
							$("#" + nsItems[j].classId).text(obj.hdbs[i].value + " " + nsItems[j].units);
							break;
						}
					}
				}
			} catch (e) {
			}
		}
	});
	try{
		$.get("sics/rest/status", {"timestamp" : new Date().getTime()}, function(data,status){
			if (status == "success") {
				$("#connection").text("OK");
				$("#connection").css("color", "green");
				var obj = jQuery.parseJSON(data);
				$("#sicsServer").text(obj.status);
				if (obj.status == "EAGER TO EXECUTE") {
					$("#sicsServer").css("color", "#00c400");
				} else if (obj.status == "COUNTING" || obj.status == "WAIT" || obj.status == "DRIVING") {
					$("#sicsServer").css("color", "#FFA500");
				} else if (obj.status == "PAUSED" || obj.status == "PAUSE") {
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
		if (typeof batchEnabled !== 'undefined' && batchEnabled) {
			$.get("sics/rest/batch", {"timestamp" : new Date().getTime()}, function(data,status){
				if (status == "success") {
					var obj = jQuery.parseJSON(data);
					$("#runnerStatus").text(obj.status);
					if (obj.status == "IDLE") {
						$("#runnerStatus").css("color", "#00c400");
					} else if (obj.status == "EXECUTING") {
						$("#runnerStatus").css("color", "#FFA500");
					} else if (obj.status == "PREPARING") {
						$("#runnerStatus").css("color", "#0000c4");
					} else  {
						$("#runnerStatus").text("DISCONNECTED");
						$("#runnerStatus").css("color", "#c40000");
					}
					if (obj.status == "EXECUTING") {
						$("#tclScript").text(obj.name);
						$("#runningCode").text(obj.text);
						try {
							var ct = obj.content;
							ct = ct.replace(/\n\n/g, '\n');
							var range = obj.range;
							var items = range.split("=");
							var newCt = '<div class="div-highlight">' + ct.substring(0, Number(items[2])) + '</div>';
							if (Number(items[2] < ct.length)) {
								var torun = ct.substring(Number(items[2]) + 1, ct.length);
								newCt += '<div class="div-normal">' + ct.substring(Number(items[2]) + 1, ct.length) + '</div>';
							}
							$("#scriptContent").html(newCt);
							var scrollPosition = Number(items[3]);
							if (scrollPosition > 4){
								scrollPosition = scrollPosition - 4;
							} else {
								scrollPosition = 0;
							}
							$("#scriptContent").scrollTop(scrollPosition * 16);
						} catch (e) {
						}
					} else {
						$("#tclScript").text("--");
						$("#runningCode").text("--");
						$("#scriptContent").text("");
						$("#scriptContent").setSelection(0, 0);
					}
				} 
			});
		}
		if (typeof timeEstimationEnabled !== 'undefined' && timeEstimationEnabled) {
			try{
				$.get("sics/rest/hdb/experiment/gumtree_time_estimate", {"timestamp" : new Date().getTime()}, function(data,status){
					if (status == "success") {
						var obj = jQuery.parseJSON(data);
						var timeFloat = 0;
						try {
							timeFloat = parseFloat(obj.value)
						} catch (e) {
						}
						if (lastTimeEstimation == timeFloat){
							return;
						}
						lastTimeEstimation = timeFloat;
						if (timeFloat == 0) {
							$("#gumtree_time_countdown").countdown('destroy');
							$("#gumtree_time_countdown").text("--");
							$("#gumtree_time_estimate").text("N/A");
							return;
						}
						var newDate = new Date();
						newDate.setTime(Math.round(timeFloat*1000));
						$("#gumtree_time_estimate").text(formatDate(newDate));
						$("#gumtree_time_countdown").countdown('destroy');
						$("#gumtree_time_countdown").countdown({until: newDate, compact: true, format: 'HMS'});
					} 
				});
			} catch (e) {
				
			}
		}
		if (!isMobileBrowser && histmemUrl != null) {
			
			var newHistmemUrl;
			if (typeof histmemTypes !== 'undefined' && histmemTypes.length > 0) {
				newHistmemUrl = histmemUrl.replace('$HISTMEM_TYPE', $('#histmem_type').val());
			} else {
				newHistmemUrl = histmemUrl;
			}
			newHistmemUrl += "&timestamp=" + new Date().getTime()

			try{
				$.get(newHistmemUrl, function(data,status){
					if (status == "success") {
						$("#histmemImage").attr("src", newHistmemUrl);
					} 
				});
			} catch (e) {
			}
		}
//		$.support.cors = true;
//		$.ajax({
//			type: "POST",
//			url: "http://localhost:61051/mobile.html",
//			data: '<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">\
//			    	<soap:Body xmlns:ns1="http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl">\
//						<ns1:getReactorPowerElement/>\
//					</soap:Body>\
//				</soap:Envelope>',
//			crossDomain: true,
//			processData: false,
//			dataType: "xml",
//			contentType: "text/xml"
//		}).done(function(msg){
//			alert(msg);
//		}).fail(function(jqXHR, textStatus, errorThrown){
//			var items = [];
//			for (var item in jqXHR) {
//				items.push(item);
//			}
//			alert(jqXHR.getAllResponseHeaders());
//		});
//		try{
//		var power = WebServiceAppServiceSoapHttpPort_getReactorPower();
//		alert(power);
//		} catch(e){
//			alert(e);
//		}
	} catch (e) {
		alert("overall error");
	}
	var dUrl = "";
	var cUrl = "";
	for(var i = 0; i < devices.length; i++) {
		for ( var j = 0; j < devices[i].items.length; j++) {
			if (devices[i].items[j].deviceId != null) {
				if (devices[i].items[j].deviceId.indexOf("/") != -1) {
					cUrl += devices[i].items[j].deviceId;
					cUrl += ",";
				} else {
					dUrl += devices[i].items[j].deviceId;
					dUrl += ",";
				}
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
								if (devices[i].items[j].adapt != null) {
									$("#" + devices[i].items[j].classId).text(devices[i].items[j].adapt(obj.hdbs[k].value) + " " + devices[i].items[j].units);
								} else {
									$("#" + devices[i].items[j].classId).text(obj.hdbs[k].value + " " + devices[i].items[j].units);
								}
								if (devices[i].items[j].colorList != null) {
									$.each(devices[i].items[j].colorList, function(key, value) {
										if (obj.hdbs[k].value == key) {
											$("#" + devices[i].items[j].classId).css("color", value);
										}
									});
								}
							} catch (e) {
							}
							break;
						} else {
							if (devices[i].items[j].deviceId == obj.hdbs[k].path){
								try{
									if (devices[i].items[j].adapt != null) {
										$("#" + devices[i].items[j].classId).text(devices[i].items[j].adapt(obj.hdbs[k].value) + " " + devices[i].items[j].units);
									} else {
										$("#" + devices[i].items[j].classId).text(obj.hdbs[k].value + " " + devices[i].items[j].units);
									}
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
	
	var url = "sics/rest/group?path=" + encodeURIComponent("/control");
	$.get(url,function(data,status){
		if (status == "success") {
			var obj = jQuery.parseJSON(data);
			if (obj.hdbs.length > 0) {
				if (!evEnabled) {
					$("#deviceList").append('<li class="ui-li ui-li-divider ui-bar-d ui-first-child" role="heading" data-role="list-divider">ENVIRONMENT CONTROLS</li>');
					for ( var i = 0; i < obj.hdbs.length; i++) {
						$("#deviceList").append('<li class="ui-li ui-li-static ui-btn-up-c"><div class="div-inlist-left">' + obj.hdbs[i].id + ': </div> <div class="div-inlist" id="' + obj.hdbs[i].id + '">' + obj.hdbs[i].value + '</div></li>');
					}
					evEnabled = true;
				} else {
					for ( var i = 0; i < obj.hdbs.length; i++) {
						$("#" + obj.hdbs[i].id).text(obj.hdbs[i].value);
					}
				}
			}
		}
	});
	
//	url = "sics/rest/group?path=" + encodeURIComponent("/control");
//	$.get(url,function(data,status){
//		if (status == "success") {
//			alert(data);
//		}
//	});
};

var timerObject = {
		interval_id : null
};

jQuery(document).ready(function(){
	$(document).attr("title", title);
	$('#titleString').text(title);
	
	if (typeof batchEnabled !== 'undefined' && batchEnabled) {
		$("#serviceList").append('<li data-role="list-divider">TCL Batch Runner</li>');
		$("#serviceList").append('<li><div class="div-inlist-left">Runner Status:</div> <div class="div-inlist" id="runnerStatus">--</div></li>');
		$("#serviceList").append('<li><div class="div-inlist-left">Script Name:</div> <div class="div-inlist" id="tclScript">--</div></li>');
		$("#serviceList").append('<li><div class="div-inlist-left">Running Code:</div> <div class="div-inlist" id="runningCode">--</div></li>');
		$("#serviceList").append('<li><div class="div-inlist-left">Script Content:</div> <div class="div-inlist" id="runningCode"><div id="scriptContent" class="div-textarea" name="textarea"></div></div></li>');
	}
	if (typeof timeEstimationEnabled !== 'undefined' && timeEstimationEnabled) {
		$("#deviceList").append('<li class="ui-li ui-li-divider ui-bar-d ui-first-child" role="heading" data-role="list-divider">TIME ESTIMATION</li>');
		$("#deviceList").append('<li class="ui-li ui-li-static ui-btn-up-c"><div class="div-inlist-left">Expected Finishing Time: </div> <div class="div-inlist" id="gumtree_time_estimate">--</div></li>');
		$("#deviceList").append('<li class="ui-li ui-li-static ui-btn-up-c"><div class="div-inlist-left">Count Down Timer: </div> <div class="div-inlist" id="gumtree_time_countdown">--</div></li>');
	}
	$("#deviceList").append('<li class="ui-li ui-li-divider ui-bar-d ui-first-child" role="heading" data-role="list-divider">NEUTRON SOURCE</li>');
	for (i = 0; i < nsItems.length; i++) {
		$("#deviceList").append('<li class="ui-li ui-li-static ui-btn-up-c"><div class="div-inlist-left">' + nsItems[i].title + ': </div> <div class="div-inlist" id="' + nsItems[i].classId + '">--</div></li>');
	}
	
	for (i = 0; i < devices.length; i++) {
		$("#deviceList").append('<li class="ui-li ui-li-divider ui-bar-d ui-first-child" role="heading" data-role="list-divider">' + devices[i].group + '</li>');
		for ( var j = 0; j < devices[i].items.length; j++) {
			$("#deviceList").append('<li class="ui-li ui-li-static ui-btn-up-c"><div class="div-inlist-left">' + devices[i].items[j].title + ': </div> <div class="div-inlist" id="' + devices[i].items[j].classId + '">--</div></li>');
		}
	}

	$('#deviceList').listview('refresh');
	$('#serviceList').listview('refresh');
	
	$("#getDom").click(function() {
		refresh();
	});

	if (histmemUrl != null) {
		if (isMobileBrowser) {
			$("#histmemDiv").html('<div data-corners="true" data-shadow="true" data-iconshadow="true" data-wrapperels="span" data-theme="c" data-disabled="false" class="ui-btn ui-shadow ui-btn-corner-all ui-btn-up-c" aria-disabled="false"><span class="ui-btn-inner"><span class="ui-btn-text">Get histogram snapshot</span></span><button id="histmemButton" class="ui-btn-hidden" data-disabled="false">Get histogram snapshot</button></div>');
		} else {
			var newHistmemUrl = histmemUrl;
			var html = "";
			if (typeof histmemTypes !== 'undefined' && histmemTypes.length > 0) {
				var defaultType = histmemTypes[0].id;
				html = '<label for="histmem_type" class="select">Select histogram type: </label><select id="histmem_type" name="histmem_type" onchange="changeHistmemType()">';
				for ( var i = 0; i < histmemTypes.length; i++) {
//					html += '<option value="' + histmemTypes[i].id + '">' + histmemTypes[i].text + '</option>';
					html += '<option value="' + histmemTypes[i].id + '"' + (histmemTypes[i].isDefault ? ' selected' : '') + '>' + histmemTypes[i].text + '</option>';
					if (histmemTypes[i].isDefault) {
						defaultType = histmemTypes[i].id;
					}
				}
				html += '</select><br>';
				newHistmemUrl = histmemUrl.replace('$HISTMEM_TYPE', defaultType);
			}
			$("#histmemDiv").html(html + '<img id="histmemImage" src="' + newHistmemUrl + '" alt="Loading error. Please refresh again.">');
		}
	}
	
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
	
	jQuery.support.cors = true;
	$.support.cors = true;
	
	
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
