var title = "Spatz";
var batchEnabled = false;
var timeEstimationEnabled = false;

function adaptAtt(val) {
	return String(Math.round(Number(val)));
}

function adaptBs(val) {
	var pos = Number(val);
	if (val < 66 && val > 62) {
		return "in";
	} else {
		return "out";
	}
}

function adaptSamNum(text) {
	var val = Number(text);
	var samNum = -1;
	for(var j = 0; j < sampleMap.length; j++) {
		if (val > sampleMap[j]) {
			if (j > 0) {
				samNum = j - (val - sampleMap[j]) / (sampleMap[j - 1] - sampleMap[j]);
			}
			break;
		}
	}
	if (samNum < 0.05 || samNum > 10.95) {
		return "out";
	} else {
		samNum = samNum.toFixed(1);
		if (samNum == Math.round(samNum)) {
			return String(Math.round(samNum));
		} else {
			return String(samNum);
		}
	}
}

var devices = [
               {"group":"NEUTRON BEAM", 
            	   "items":[{"classId":"bm1_counts", "deviceId":"bm1_counts", "title":"Monitor 1", "units":"cts"},
            		   		{"classId":"bm1_event_rate", "deviceId":"bm1_event_rate", "title":"Monitor 1 rate", "units":"cts/s"}, 
            	            {"classId":"detector_counts", "deviceId":"/instrument/detector/total_counts", "title":"Detector Counts", "units":"cts"}
            	            ]
               },
               {"group":"CHOPPERS", 
            	   "items":[{"classId":"c01_spee", "deviceId":"/instrument/chopper/c01/spee", "title":"C01 Speed", "units":"rpm"},
            	            {"classId":"c01_phas", "deviceId":"/instrument/chopper/c01/phas", "title":"C01 phase offset", "units":"\u00B0", "decimal":2},
            	            {"classId":"c02_spee", "deviceId":"/instrument/chopper/c02/spee", "title":"C02 Speed", "units":"rpm"},
            	            {"classId":"c02_phas", "deviceId":"/instrument/chopper/c02/phas", "title":"C02 phase offset", "units":"\u00B0", "decimal":2},
            	            {"classId":"c2b_spee", "deviceId":"/instrument/chopper/c2b/spee", "title":"C2b Speed", "units":"rpm"},
            	            {"classId":"c2b_phas", "deviceId":"/instrument/chopper/c2b/phas", "title":"C2b phase offset", "units":"\u00B0", "decimal":2},
            	            {"classId":"c03_spee", "deviceId":"/instrument/chopper/c03/spee", "title":"C03 Speed", "units":"rpm"},
            	            {"classId":"c03_phas", "deviceId":"/instrument/chopper/c03/phas", "title":"C03 phase offset", "units":"\u00B0", "decimal":2}
            	            ]
               },
               {"group":"SLITS", 
            	   "items":[{"classId":"ss2hg", "deviceId":"ss2hg", "title":"s2 horizontal gap", "units":"mm", "decimal":2},
            		   		{"classId":"ss3hg", "deviceId":"ss3hg", "title":"s3 horizontal gap", "units":"mm", "decimal":2},
            		   		{"classId":"ss4hg", "deviceId":"ss4hg", "title":"s4 horizontal gap", "units":"mm", "decimal":2},
            		   		{"classId":"ss2vg", "deviceId":"ss2vg", "title":"s2 vertical gap", "units":"mm", "decimal":2},
            		   		{"classId":"ss3vg", "deviceId":"ss3vg", "title":"s3 vertical gap", "units":"mm", "decimal":2},
            		   		{"classId":"ss4vg", "deviceId":"ss4vg", "title":"s4 vertical gap", "units":"mm", "decimal":2}
            	            ]
               },
               {"group":"SAMPLE STAGE", 
            	   "items":[{"classId":"som", "deviceId":"som", "title":"Sample Omega", "units":"degrees", "decimal":3},
            	            {"classId":"stilt", "deviceId":"stilt", "title":"Sample Tilt", "units":"mm", "decimal":3},
            	            {"classId":"sx", "deviceId":"sx", "title":"Sample X", "units":"mm", "decimal":3},
            	            {"classId":"sxtop", "deviceId":"sxtop", "title":"Sample X Top", "units":"mm", "decimal":3},
            	            {"classId":"sy", "deviceId":"sy", "title":"Sample Y", "units":"mm", "decimal":3},
            	            {"classId":"detrot", "deviceId":"detrot", "title":"Detector Rotation", "units":"degrees", "decimal":3}
            	            ]
               }
               ];

var nsItems = [
               {"classId":"reactorPower", "deviceId":"reactorPower", "title":"Reactor Power", "units":"MW"},
               {"classId":"cnsInTemp", "deviceId":"cnsInTemp", "title":"CNS In Temp", "units":"K"},
               {"classId":"cnsOutTemp", "deviceId":"cnsOutTemp", "title":"CNS Out Temp", "units":"K"}
               ];

//var histmemUrl = "dae/rest/image?type=TOTAL_HISTOGRAM_XY&screen_size_x=800&screen_size_y=600";
var histmemUrl = "dae/rest/image?type=$HISTMEM_TYPE&screen_size_x=800";

var histmemTypes = [
                    {"id" : "TOTAL_HISTOGRAM_XY", "text" : "Total x-y histogram", "isDefault" : true},
                    {"id" : "TOTAL_HISTOGRAM_X", "text" : "Total x histogram"},
                    {"id" : "TOTAL_HISTOGRAM_Y", "text" : "Total y histogram"},
                    ];