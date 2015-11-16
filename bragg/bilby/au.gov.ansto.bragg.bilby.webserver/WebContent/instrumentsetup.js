var title = "Bilby";
var batchEnabled = false;
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

function adaptAtt(val) {
	return String(Math.round(Number(val)));
}

function adaptBs(val) {
	var pos = Number(val);
	if (val < 64 && val > 62) {
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
            	   "items":[{"classId":"monitor_counts", "deviceId":"monitor_counts", "title":"Monitor", "units":"cts"}, 
            	            {"classId":"detector_counts", "deviceId":"/instrument/detector/total_counts", "title":"Detector Counts", "units":"c"}, 
            	            {"classId":"total_detector_rate", "deviceId":"::histogram_memory::ratemap_xy_total", "title":"Tot. Rate on Detector", "units":"c/t", "decimal":2}, 
            	            {"classId":"xy_max_binrate", "deviceId":"::histogram_memory::ratemap_xy_max_bin", "title":"Max Rate on Pixel", "units":"c/t", "decimal":2}
            	            ]
               },
               {"group":"ATTENUATOR", 
            	   "items":[{"classId":"att", "deviceId":"att", "title":"att", "units":"", "decimal":0}
            	            ]
               },
               {"group":"VELOCITY SELECTOR", 
            	   "items":[{"classId":"vs_pos", "deviceId":"vs_pos", "title":"position", "units":""},
            	            {"classId":"vs_lambda", "deviceId":"vs_lambda", "title":"wavelength", "units":"\u212B", "decimal":2},
            	            {"classId":"vs_speed", "deviceId":"vs_speed", "title":"speed", "units":"rpm", "decimal":0}
            	            ]
               },
               {"group":"CHOPPERS", 
            	   "items":[{"classId":"t0_chopper_id", "deviceId":"t0_chopper_id", "title":"T0_chopper_id", "units":""},
            	            {"classId":"t0_chopper_freq", "deviceId":"t0_chopper_freq", "title":"T0_chopper_frequency", "units":"Hz", "decimal":2},
            	            {"classId":"master1_chopper_id", "deviceId":"master1_chopper_id", "title":"master1_chopper_id", "units":""},
            	            {"classId":"master2_chopper_id", "deviceId":"master2_chopper_id", "title":"master2_chopper_id", "units":""},
            	            {"classId":"master_chopper_freq", "deviceId":"master_chopper_freq", "title":"master_chopper_frequency", "units":"Hz", "decimal":2},
            	            {"classId":"gs_l1", "deviceId":"gs_l1", "title":"L1", "units":"mm", "decimal":1}
            	            ]
               },
               {"group":"DETECTOR", 
            	   "items":[{"classId":"gs_l2_curtainl", "deviceId":"gs_l2_curtainl", "title":"L2_curtaindet_left", "units":"mm", "decimal":1},
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

var nsItems = [
               {"classId":"reactorPower", "deviceId":"reactorPower", "title":"Reactor Power", "units":"MW"},
               {"classId":"cnsInTemp", "deviceId":"cnsInTemp", "title":"CNS In Temp", "units":"K"},
               {"classId":"cnsOutTemp", "deviceId":"cnsOutTemp", "title":"CNS Out Temp", "units":"K"}
               ];

var histmemUrl = "dae/rest/image?type=TOTAL_HISTOGRAM_XY&screen_size_x=800&screen_size_y=600";