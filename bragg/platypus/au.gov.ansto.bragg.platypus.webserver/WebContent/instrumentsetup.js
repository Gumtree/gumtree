var title = "Platypus";
var batchEnabled = false;
var useNewProxy = true;
var timeEstimationEnabled = false;
var REVERSE_CATALOG_ENABLED = true;
var _lastModified = "0";
var devices = [
			   {"group":"SHUTTER STATUS", 
			 	   "items":[
			 		   		{"classId":"tertiary", "deviceId":"/instrument/status/tertiary", "title":"Tertiary Shutter", "units":""}
			 		   		]
			   },
               {"group":"NEUTRON COUNTS", 
            	   "items":[{"classId":"total_counts", "deviceId":"::histogram_memory::total_counts", "title":"Detector Counts", "units":"ct"}, 
            	            {"classId":"histogram_memory_time", "deviceId":"::histogram_memory::time", "title":"Time of Counting", "units":"s"}, 
            	            {"classId":"bm1_counts", "deviceId":"bm1_counts", "title":"BM1 Counts", "units":"ct"},
            	            {"classId":"bm2_counts", "deviceId":"bm2_counts", "title":"BM2 Counts", "units":"ct"},
            	            {"classId":"ratemap_xy_total", "deviceId":"::histogram_memory::ratemap_xy_total", "title":"Detector Rate", "units":"ct"}
            	            ]
               },
               {"group":"CHOPPER", 
            	   "items":[{"classId":"ch1speed", "deviceId":"/instrument/disk_chopper/ch1speed", "title":"chopper1 speed", "units":"rpm"}
            	   ]
               },
               {"group":"SLITS", 
            	   "items":[
            	            {"classId":"s1vg", "deviceId":"/instrument/slits/first/vertical/gap", "title":"First vertical gap", "units":"mm"},
            	            {"classId":"s2vg", "deviceId":"/instrument/slits/second/vertical/gap", "title":"Second vertical gap", "units":"mm"},
            	            {"classId":"s3vg", "deviceId":"/instrument/slits/third/vertical/gap", "title":"Third vertical gap", "units":"mm"},
            	            {"classId":"s4vg", "deviceId":"/instrument/slits/fourth/vertical/gap", "title":"Fourth vertical gap", "units":"mm"}
            	            ]
               },
               {"group":"SAMPLE", 
            	   "items":[
            		   {"classId":"sample_name", "deviceId":"/sample/name", "title":"Name", "units":""},
            		   {"classId":"file_name", "deviceId":"/experiment/file_name", "title":"Filename", "units":""},
            		   {"classId":"sth", "deviceId":"/sample/sth", "title":"sth", "units":"degrees"},
            		   {"classId":"sztop", "deviceId":"/sample/sztop", "title":"sztop", "units":"mm"},
            		   {"classId":"sxtop", "deviceId":"/sample/sxtop", "title":"sxtop", "units":"mm"},
            		   {"classId":"translate_z", "deviceId":"/sample/translate_z", "title":"translate_z", "units":"mm"},
            		   {"classId":"translate_x", "deviceId":"/sample/translate_x", "title":"translate_x", "units":"mm"}
            	   ]
               }
               ];

var nsItems = [
               {"classId":"reactorPower", "deviceId":"reactorPower", "title":"Reactor Power", "units":"MW"},
               {"classId":"cnsInTemp", "deviceId":"cnsInTemp", "title":"CNS In Temp", "units":"K"},
               {"classId":"cnsOutTemp", "deviceId":"cnsOutTemp", "title":"CNS Out Temp", "units":"K"}
               ];

//var histmemUrl = "dae/rest/image?type=TOTAL_HISTOGRAM_XT&screen_size_x=760&screen_size_y=760";

var histmemUrl = "dae/rest/image?type=$HISTMEM_TYPE";

var histmemTypes = [
				    {"id" : "TOTAL_HISTOGRAM_T", "text" : "Total t histogram"},
				    {"id" : "TOTAL_HISTOGRAM_XT", "text" : "Total x-t histogram"},
				    {"id" : "TOTAL_HISTOGRAM_YT", "text" : "Total y-t histogram", "isDefault" : true},
				    {"id" : "RAW_TOTAL_HISTOGRAM_XT", "text" : "Raw x-t histogram"},
				    {"id" : "RAW_TOTAL_HISTOGRAM_YT", "text" : "Raw y-t histogram"}
                    ];

function loadResource() {
	const url = "resource?name=igorscript&type=json&ts=" + _lastModified;
	$.get(url, function(data) {
		if (data["status"] == "OK") {
			var text = data["text"];
			_lastModified = data["ts"];
			text = text.replaceAll("\n", "<br>");
			const newCt = $('<pre/>').html(text);
			$("#scriptContent").html(newCt);
		} else if (data["status"] == "unchanged") {
			console.log("unchanged");
		} else {
			$("#scriptContent").html('<div class="div-highlight">' + data["reason"] + '</div>');
		}
	});
}