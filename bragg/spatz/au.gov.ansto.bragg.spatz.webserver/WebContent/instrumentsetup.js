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
            	   "items":[{"classId":"t0_chopper_id", "deviceId":"t0_chopper_id", "title":"T0_chopper_id", "units":""},
            	            {"classId":"t0_chopper_freq", "deviceId":"t0_chopper_freq", "title":"T0_chopper_frequency", "units":"Hz", "decimal":2},
            	            {"classId":"master1_chopper_id", "deviceId":"master1_chopper_id", "title":"master1_chopper_id", "units":""},
            	            {"classId":"master2_chopper_id", "deviceId":"master2_chopper_id", "title":"master2_chopper_id", "units":""},
            	            {"classId":"master_chopper_freq", "deviceId":"master_chopper_freq", "title":"master_chopper_frequency", "units":"Hz", "decimal":2}
            	            ]
               },
               {"group":"SLITS", 
            	   "items":[{"classId":"s2left", "deviceId":"s2left", "title":"s2 left", "units":"", "mm":0},
            		   		{"classId":"s2right", "deviceId":"s2right", "title":"s2 right", "units":"", "mm":0},
            		   		{"classId":"s2top", "deviceId":"s2top", "title":"s2 top", "units":"", "mm":0},
            		   		{"classId":"s2bot", "deviceId":"s2bot", "title":"s2 bottom", "units":"", "mm":0},
            		   		{"classId":"s3left", "deviceId":"s3left", "title":"s3 left", "units":"", "mm":0},
            		   		{"classId":"s3right", "deviceId":"s3right", "title":"s3 right", "units":"", "mm":0},
            		   		{"classId":"s3top", "deviceId":"s3top", "title":"s3 top", "units":"", "mm":0},
            		   		{"classId":"s3bot", "deviceId":"s3bot", "title":"s3 bottom", "units":"", "mm":0}
            	            ]
               },
               {"group":"SAMPLE STAGE", 
            	   "items":[{"classId":"som", "deviceId":"som", "title":"Sample Omega", "units":"deg"},
            	            {"classId":"stilt", "deviceId":"stilt", "title":"Sample Tilt", "units":"mm"},
            	            {"classId":"sx", "deviceId":"sx", "title":"Sample X", "units":"mm"},
            	            {"classId":"sxtop", "deviceId":"sxtop", "title":"Sample X Top", "units":"mm"},
            	            {"classId":"sy", "deviceId":"sy", "title":"Sample Y", "units":"mm"}
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