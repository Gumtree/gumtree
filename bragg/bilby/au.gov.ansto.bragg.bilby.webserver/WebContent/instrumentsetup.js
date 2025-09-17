var title = "Bilby";
var batchEnabled = false;
var timeEstimationEnabled = true;
var useNewProxy = true;
var sampleMap = [
					250,
					210.500,
					168.375,
					126.250,
					84.125,
					42.000,
					-39.750,
					-81.875,
					-124.000,
					-166.125,
					-208.250,
					-250
				];

sampleMap = [
	       		455.0,
	       		385.0,
	       		315.0,
	       		245.0, 
	       		175.0,
	       		105.0,
	       		35.0,
	       		-35.0,
	       		-105.0,
	       		-175.0,
	       		-245.0,
	       		-315.0,
	       		-385.0,
	       		-455.0
	       ];

sampleMap = [
	        510.0,
	        450.0,
	        390.0,
	        330.0,
	        270.0,
	        210.0,
	        150.0,
	        90.0,
	        30.0,
	        -30.0,
	        -90.0,
	        -150.0,
	        -210.0,
	        -270.0,
	        -330.0,
	        -390.0,
	        -450.0,
	        -510.0
	       ];


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
	if (samNum < 0.05 || samNum > sampleMap.length - 1.05) {
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
			   {"group":"NEUTRON SOURCE", 
			 	   "items":[{"classId":"reactorPower", "deviceId":"/instrument/source/power", "title":"Reactor Power", "units":"MW"},
			 		   		{"classId":"cnsOutTemp", "deviceId":"/instrument/source/cns_out", "title":"CNS Out Temp", "units":"K"}
			 	            ]
			   },
               {"group":"NEUTRON BEAM", 
            	   "items":[{"classId":"monitor_counts", "deviceId":"monitor_counts", "title":"Monitor", "units":"cts"}, 
            	            {"classId":"detector_counts", "deviceId":"/instrument/detector/total_counts", "title":"Detector Counts", "units":"c"}, 
            	            {"classId":"total_detector_rate", "deviceId":"::histogram_memory::ratemap_xy_total", "title":"Tot. Rate on Detector", "units":"c/t", "decimal":2}, 
            	            {"classId":"xy_max_binrate", "deviceId":"::histogram_memory::ratemap_xy_max_bin", "title":"Max Rate on Pixel", "units":"c/t", "decimal":2}
            	            ]
               },
               {"group":"SHUTTER STATUS", 
            	   "items":[{"classId":"secondary", "deviceId":"/instrument/sis/status/secondary", "title":"Secondary Shutter", "units":""},
            		   		{"classId":"tertiary", "deviceId":"/instrument/sis/status/tertiary", "title":"Tertiary Shutter", "units":""},
            		   		{"classId":"fast_shutter", "deviceId":"/instrument/sis/status/fast_shutter", "title":"Fast Shutter", "units":""}
            	            ]
               },
               {"group":"ATTENUATOR", 
            	   "items":[{"classId":"att_pos", "deviceId":"att_pos", "title":"att position", "units":"", "decimal":0}
            	            ]
               },
               {"group":"VELOCITY SELECTOR", 
            	   "items":[
            		   		// {"classId":"vs_pos", "deviceId":"vs_pos", "title":"position", "units":""},
            	            {"classId":"vs_lambda", "deviceId":"/instrument/nvs067/status/wavelength", "title":"wavelength", "units":"\u212B", "decimal":1},
            	            {"classId":"vs_speed", "deviceId":"/instrument/nvs067/status/rpm", "title":"speed", "units":"rpm", "decimal":1}
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
               {"group":"DETECTORS", 
            	   "items":[{"classId":"gs_l1", "deviceId":"gs_l1", "title":"L1", "units":"mm", "decimal":1},
            	            {"classId":"gs_l2_curtainl", "deviceId":"gs_l2_curtainl", "title":"L2_curtaindet_left", "units":"mm", "decimal":1},
            	            {"classId":"gs_l2_curtainr", "deviceId":"gs_l2_curtainr", "title":"L2_curtaindet_right", "units":"mm", "decimal":1},
            	            {"classId":"gs_l2_curtainu", "deviceId":"gs_l2_curtainu", "title":"L2_curtaindet_up", "units":"mm", "decimal":1},
            	            {"classId":"gs_l2_curtaind", "deviceId":"gs_l2_curtaind", "title":"L2_curtaindet_down", "units":"mm", "decimal":1},
            	            {"classId":"curtainl", "deviceId":"curtainl", "title":"curtainl", "units":"mm", "decimal":1},
            	            {"classId":"curtainr", "deviceId":"curtainr", "title":"curtainr", "units":"mm", "decimal":1},
            	            {"classId":"curtainu", "deviceId":"curtainu", "title":"curtainu", "units":"mm", "decimal":1},
            	            {"classId":"curtaind", "deviceId":"curtaind", "title":"curtaind", "units":"mm", "decimal":1},
            	            {"classId":"gs_l2_det", "deviceId":"gs_l2_det", "title":"L2_det", "units":"mm", "decimal":1}
            	            ]
               },
               {"group":"BEAM STOP", 
            	   "items":[{"classId":"bs3", "deviceId":"bs3", "title":"BS3", "units":"", "adapt":adaptBs},
            	            {"classId":"bs4", "deviceId":"bs4", "title":"BS4", "units":"", "adapt":adaptBs},
            	            {"classId":"bs5", "deviceId":"bs5", "title":"BS5", "units":"", "adapt":adaptBs}
            	            ]
               },
               {"group":"SAMPLE", 
            	   "items":[{"classId":"samplename", "deviceId":"samplename", "title":"Sample Name", "units":""},
            	            {"classId":"samx", "deviceId":"samx", "title":"Sample Number", "units":"", "adapt":adaptSamNum}
            	            ]
               },
               {"group":"GUIDE", 
            	   "items":[{"classId":"gs_nguide", "deviceId":"gs_nguide", "title":"Guide Setup", "units":""}
            	            ]
               }
               ];

//var nsItems = [
//               {"classId":"reactorPower", "deviceId":"/instrument/source/power", "title":"Reactor Power", "units":"MW"},
//               {"classId":"cnsInTemp", "deviceId":"/instrument/source/", "title":"CNS In Temp", "units":"K"},
//               {"classId":"cnsOutTemp", "deviceId":"/instrument/source/cns_out", "title":"CNS Out Temp", "units":"K"}
//               ];
var nsItems = [
    
    ];

//var histmemUrl = "dae/rest/image?type=TOTAL_HISTOGRAM_XY&screen_size_x=800&screen_size_y=600";
var histmemUrl = "dae/rest/image?type=$HISTMEM_TYPE&scaling_type=LOG&screen_size_x=800";

var histmemTypes = [
                    {"id" : "TOTAL_HISTOGRAM_XY", "text" : "Total x-y histogram", "isDefault" : true},
                    {"id" : "TOTAL_HISTOGRAM_XT", "text" : "Total x-t histogram"},
                    {"id" : "TOTAL_HISTOGRAM_YT", "text" : "Total y-t histogram"},
                    {"id" : "TOTAL_HISTOGRAM_X", "text" : "Total x histogram"},
                    {"id" : "TOTAL_HISTOGRAM_Y", "text" : "Total y histogram"},
                    {"id" : "TOTAL_HISTOGRAM_T", "text" : "Total t histogram"}
                    ];