var title = "Kookaburra";
var useNewProxy = true;
var batchEnabled = false;
var LOCK_ENABLED = true;
var REVERSE_CATALOG_ENABLED = true;

var apselMap = [
	-65.5,
	0,
	65.5,
	131,
	196.5
];

function adaptApsel(text) {
	var val = Number(text);
	var samNum = -1;
	for(var j = 0; j < apselMap.length; j++) {
		if (val < apselMap[j]) {
			if (j > 0) {
				samNum = j - (apselMap[j] - val) / (apselMap[j] - apselMap[j - 1]);
			}
			break;
		}
	}
	if (samNum < 0.05 || samNum > 3.95) {
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

function adaptPolyShield(text) {
	if (text != null && (text == "1" || text.toLowerCase() == "in")) {
		return "IN";
	} else {
		return "OUT";
	}
}

var devices = [
               {"group":"NEUTRON BEAM", 
            	   "items":[{"classId":"plc_secondary", "deviceId":"plc_secondary", "title":"Secondary Shutter", "units":""},
            	            {"classId":"plc_tertiary", "deviceId":"plc_tertiary", "title":"Sample Shutter", "units":""},
            	            {"classId":"polyshield", "deviceId":"/instrument/GreenPolyShield/greenpolyshield", "title":"Green Polyshield", "units":"", "adapt":adaptPolyShield},
            	            {"classId":"bm1_counts", "deviceId":"bm1_counts", "title":"BM1 Counts", "units":"counts"},
            	            {"classId":"bm1_event_rate", "deviceId":"bm1_event_rate", "title":"BM1 Event Rate", "units":"counts/sec", "decimal":3},
            	            {"classId":"bm2_counts", "deviceId":"bm2_counts", "title":"BM2 Counts", "units":"counts"},
            	            {"classId":"bm2_event_rate", "deviceId":"bm2_event_rate", "title":"BM2 Event Rate", "units":"counts/sec", "decimal":3},
            	            {"classId":"bm3_counts", "deviceId":"bm3_counts", "title":"BM3 Counts", "units":"counts"},
            	            {"classId":"bm3_event_rate", "deviceId":"bm3_event_rate", "title":"BM3 Event Rate", "units":"counts/sec", "decimal":3},
//            	            {"classId":"monitor_counts", "deviceId":"monitor_counts", "title":"Monitor", "units":"cts"}, 
//            	            {"classId":"detector_counts", "deviceId":"/instrument/detector/total_counts", "title":"Detector Counts", "units":"c"}, 
//            	            {"classId":"total_detector_rate", "deviceId":"::histogram_memory::ratemap_xy_total", "title":"Tot. Rate on Detector", "units":"c/t"}, 
//            	            {"classId":"xy_max_binrate", "deviceId":"::histogram_memory::ratemap_xy_max_bin", "title":"Max Rate on Pixel", "units":"c/t"}
            	            ]
               },
               {"group":"PREMONOCHROMATOR", 
            	   "items":[{"classId":"pmom", "deviceId":"pmom", "title":"pmom", "units":"deg", "decimal":2},
            	            {"classId":"pmchi", "deviceId":"pmchi", "title":"pmchi", "units":"deg", "decimal":2}
            	            ]
               },
               {"group":"BE FILTER (0 = in the beam, 133 = out of the beam)", 
            	   "items":[{"classId":"bex", "deviceId":"bex", "title":"bex", "units":"mm", "decimal":2}
            	            ]
               },
               {"group":"CHANNEL-CUT MONOCHROMATOR", 
            	   "items":[{"classId":"m1om", "deviceId":"m1om", "title":"m1om", "units":"deg", "decimal":5},
            	            {"classId":"m1chi", "deviceId":"m1chi", "title":"m1chi", "units":"deg", "decimal":3},
            	            {"classId":"m1x", "deviceId":"m1x", "title":"m1x", "units":"mm", "decimal":2}
            	            ]
               },
               {"group":"PRE-SAMPLE SLIT 1", 
            	   "items":[
//            	            {"classId":"ss1u", "deviceId":"ss1u", "title":"ss1u", "units":"mm"},
//            	            {"classId":"ss1d", "deviceId":"ss1d", "title":"ss1d", "units":"mm"},
//            	            {"classId":"ss1r", "deviceId":"ss1r", "title":"ss1r", "units":"mm"},
//            	            {"classId":"ss1l", "deviceId":"ss1l", "title":"ss1l", "units":"mm"},
            	            {"classId":"ss1hg", "deviceId":"ss1hg", "title":"Horizontal Gap ss1hg", "units":"mm", "decimal":1},
//            	            {"classId":"ss1ho", "deviceId":"ss1ho", "title":"ss1ho", "units":"mm"},
            	            {"classId":"ss1vg", "deviceId":"ss1vg", "title":"Vertical Gap ss1vg", "units":"mm", "decimal":1},
//            	            {"classId":"ss1vo", "deviceId":"ss1vo", "title":"ss1vo", "units":"mm"}
            	            ]
               },
               {"group":"SAMPLE (pos-1 = 32, pos-2 = 177, pos-3 = 321.5, pos-4 = 468, pos-5 = 612.5)", 
            	   "items":[{"classId":"samz", "deviceId":"samz", "title":"samz", "units":"mm", "decimal":1},
            		   		{"classId":"samx", "deviceId":"samx", "title":"samx", "units":"mm", "decimal":1},
            	            {"classId":"apsel", "deviceId":"apsel", "title":"apselnum", "units":"", "adapt":adaptApsel},
            	            {"classId":"samplename", "deviceId":"samplename", "title":"Sample Name", "units":""},
            	            {"classId":"sampledescription", "deviceId":"sampledescription", "title":"Sample Description", "units":""}
            	            ]
               },
               {"group":"CHANNEL-CUT ANALYSER", 
            	   "items":[{"classId":"m2om", "deviceId":"m2om", "title":"m2om", "units":"deg", "decimal":5},
            	            {"classId":"m2chi", "deviceId":"m2chi", "title":"m2chi", "units":"deg", "decimal":3},
            	            {"classId":"m2x", "deviceId":"m2x", "title":"m2x", "units":"mm", "decimal":2},
            	            {"classId":"m2y", "deviceId":"m2y", "title":"m2y", "units":"mm", "decimal":2}
            	            ]
               },
//               {"group":"ATTENUATOR", 
//            	   "items":[{"classId":"att", "deviceId":"att", "title":"att", "units":"mm"}
//            	            ]
//               },
               {"group":"POST-SAMPLE SLIT 2", 
            	   "items":[
//            	            {"classId":"ss2u", "deviceId":"ss2u", "title":"ss2u", "units":"mm"},
//            	            {"classId":"ss2d", "deviceId":"ss2d", "title":"ss2d", "units":"mm"},
//            	            {"classId":"ss2r", "deviceId":"ss2r", "title":"ss2r", "units":"mm"},
//            	            {"classId":"ss2l", "deviceId":"ss2l", "title":"ss2l", "units":"mm"},
            	            {"classId":"ss2hg", "deviceId":"ss2hg", "title":"Horizontal Gap ss2hg", "units":"mm", "decimal":1},
//            	            {"classId":"ss2ho", "deviceId":"ss2ho", "title":"ss2ho", "units":"mm"},
            	            {"classId":"ss2vg", "deviceId":"ss2vg", "title":"Vertical Gap ss2vg", "units":"mm", "decimal":1},
//            	            {"classId":"ss2vo", "deviceId":"ss2vo", "title":"ss2vo", "units":"mm"}
            	            ]
               },
               {"group":"MAIN DETECTOR", 
            	   "items":[{"classId":"mdet", "deviceId":"mdet", "title":"mdet", "units":"mm", "decimal":1}
            	            ]
               },
               {"group":"SCAN", 
            	   "items":[{"classId":"currpoint", "deviceId":"currpoint", "title":"Current Point", "units":""},
            	            {"classId":"datafilename", "deviceId":"datafilename", "title":"File Name", "units":""}
            	            ]
               }
               ];

var nsItems = [
               {"classId":"reactorPower", "deviceId":"reactorPower", "title":"Reactor Power", "units":"MW"},
               {"classId":"cnsInTemp", "deviceId":"cnsInTemp", "title":"CNS In Temp", "units":"K"},
               {"classId":"cnsOutTemp", "deviceId":"cnsOutTemp", "title":"CNS Out Temp", "units":"K"}
               ];

var histmemUrl = "kookaburra/rest/plot?height=360&width=600;"
	+ "dae/rest/image?type=TOTAL_HISTOGRAM_XY&screen_size_x=600&screen_size_y=600&ROI_xmin=0&ROI_xmax=4;"
	+ "dae/rest/image?type=TOTAL_HISTOGRAM_XY&screen_size_x=600&screen_size_y=600";