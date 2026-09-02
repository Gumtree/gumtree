var title = "Wombat";
var useNewProxy = true;
var batchEnabled = true;

function adaptShutter(val) {
	if (val == 1 || val == "1") {
		return "OPEN";
	} else {
		return "CLOSED";
	}
}

var devices = [
               {"group":"BEAM STATUS", 
            	   "items":[{"classId":"sis_tertiary", "deviceId":"/instrument/sis/instrument/b00_tertiary_open", "title":"Sample Shutter", "units":"", "adapt":adaptShutter},
  	            			{"classId":"sis_secondary", "deviceId":"/instrument/sis/guide/b03_secondary_open", "title":"Secondary Shutter", "units":"", "adapt":adaptShutter},
  	            			{"classId":"bm1_counts", "deviceId":"bm1_counts", "title":"BM1 Counts", "units":"ct"},
            	            {"classId":"bm2_counts", "deviceId":"bm2_counts", "title":"BM2 Counts", "units":"ct"},
            	            {"classId":"total_counts", "deviceId":"::histogram_memory::total_counts", "title":"Detector Counts", "units":"ct"}, 
            	            {"classId":"histogram_memory_time", "deviceId":"::histogram_memory::time", "title":"Time of Counting", "units":"s"}
            	            ]
               },
               {"group":"MONOCHROMATOR", 
            	   "items":[{"classId":"mtth", "deviceId":"mtth", "title":"mtth", "units":"deg"},
            	            {"classId":"mom", "deviceId":"mom", "title":"mom", "units":"deg"},
            	            {"classId":"mf2", "deviceId":"mf2", "title":"mf2", "units":"deg"}
            	            ]
               },
               {"group":"COLLIMATOR", 
            	   "items":[{"classId":"oct", "deviceId":"oct", "title":"oct", "units":"deg"}
            	            ]
               },
               {"group":"SAMPLE STAGE", 
            	   "items":[{"classId":"stth", "deviceId":"stth", "title":"stth", "units":"deg"},  
            	            {"classId":"sx", "deviceId":"sx", "title":"sx", "units":"mm"},
            	            {"classId":"sy", "deviceId":"sy", "title":"sy", "units":"mm"},
            	            {"classId":"som", "deviceId":"som", "title":"som", "units":"deg"},
            	            {"classId":"msom", "deviceId":"/instrument/msom", "title":"msom", "units":"deg"}
            	            ]
               },
               {"group":"EULER CRADLE", 
            	   "items":[{"classId":"eom", "deviceId":"eom", "title":"eom", "units":"deg"},  
            	            {"classId":"echi", "deviceId":"echi", "title":"echi", "units":"deg"},
            	            {"classId":"ephi", "deviceId":"ephi", "title":"ephi", "units":"deg"}
            	            ]
               },
               {"group":"SLITS", 
            	   "items":[{"classId":"ss1vg", "deviceId":"ss1vg", "title":"slit1 vert gap", "units":"mm"},  
            	            {"classId":"ss1hg", "deviceId":"ss1hg", "title":"slit1 horiz gap", "units":"mm"},
            	            {"classId":"ss2vg", "deviceId":"ss2vg", "title":"slit2 vert gap", "units":"mm"},
            	            {"classId":"ss2hg", "deviceId":"ss2hg", "title":"slit2 horiz gap", "units":"mm"}
            	   ]
               },
               {"group":"MAGNET1", 
            	   "items":[{"classId":"m1_field", "deviceId":"/sample/magnet1/magnet/field", "title":"Field", "units":"tesla"},  
            		   		{"classId":"m1_he_level", "deviceId":"/sample/magnet1/magnet/he_level", "title":"He Level", "units":""},
            		   		{"classId":"m1_temp", "deviceId":"/sample/magnet1/magnet/temperature", "title":"Temperature", "units":"K"},
            		   		{"classId":"m1_Ramp_setpoint_amps", "deviceId":"/sample/magnet1/magnet/ramp_setpoint_amps", "title":"Ramp_setpoint_amps", "units":"A"},
            		   		{"classId":"m1_Ramp_setpoint_tesla", "deviceId":"/sample/magnet1/magnet/ramp_setpoint_amps", "title":"Ramp_setpoint_tesla", "units":"tesla"},
            		   		{"classId":"m1_setpoint", "deviceId":"/sample/magnet1/magnet/setpoint", "title":"Setpoint", "units":"tesla"}
            	   ]
               }
];

var nsItems = [
               {"classId":"reactorPower", "deviceId":"reactorPower", "title":"Reactor Power", "units":"MW"}
               ];

var histmemUrl = "dae/rest/image?screen_size_x=800&screen_size_y=600&scaling_type=LOG";