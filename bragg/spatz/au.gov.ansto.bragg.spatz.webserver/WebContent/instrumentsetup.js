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
            		   		{"classId":"bm2_counts", "deviceId":"bm2_counts", "title":"Monitor 2", "units":"cts"}, 
            	            {"classId":"detector_counts", "deviceId":"/instrument/detector/total_counts", "title":"Detector Counts", "units":"c"}
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
            	   "items":[{"classId":"s2ho", "deviceId":"s2ho", "title":"S2 horizontal opening ", "units":"", "mm":0},
            		   		{"classId":"s2vo", "deviceId":"s2vo", "title":"S2 vertical opening ", "units":"", "mm":0},
            		   		{"classId":"s3ho", "deviceId":"s3ho", "title":"S3 horizontal opening ", "units":"", "mm":0},
            		   		{"classId":"s3vo", "deviceId":"s3vo", "title":"S3 vertical opening ", "units":"", "mm":0},
            		   		{"classId":"s4ho", "deviceId":"s4ho", "title":"S4 horizontal opening ", "units":"", "mm":0},
            		   		{"classId":"s4vo", "deviceId":"s4vo", "title":"S4 vertical opening ", "units":"", "mm":0}
            	            ]
               },
               {"group":"MONOCHROMATOR", 
            	   "items":[{"classId":"mom", "deviceId":"mom", "title":"Omega", "units":"deg"},
            	            {"classId":"stth", "deviceId":"stth", "title":"wavelength", "units":"deg"}
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