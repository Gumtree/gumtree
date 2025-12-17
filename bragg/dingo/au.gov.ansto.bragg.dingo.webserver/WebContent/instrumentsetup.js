var title = "Dingo";
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
               {"group":"CAMERA STATUS", 
            	   "items":[{"classId":"sis_tertiary", "deviceId":"/instrument/sis/instrument/b00_tertiary_open", "title":"Sample Shutter", "units":"", "adapt":adaptShutter},
  	            			{"classId":"sis_secondary", "deviceId":"/instrument/sis/guide/b03_secondary_open", "title":"Secondary Shutter", "units":"", "adapt":adaptShutter},
        		   			{"classId":"cm1_mode", "deviceId":"cm1_mode", "title":"Camera Mode", "units":""},  
            	            {"classId":"cm1_preset", "deviceId":"cm1_preset", "title":"Camera Preset", "units":""},
            	            {"classId":"cm1_time", "deviceId":"cm1_time", "title":"Exposure Time", "units":"s"},
            	            {"classId":"cm1_counts", "deviceId":"cm1_counts", "title":"Camera Counts", "units":"cts"}
            	            ]
               },
               {"group":"INSTRUMENT CONFIGURATION", 
            	   "items":[{"classId":"dy", "deviceId":"dy", "title":"dy", "units":"mm"}
            	            ]
               },
               {"group":"SAMPLE STAGE", 
            	   "items":[  
            	            {"classId":"sx", "deviceId":"sx", "title":"sx", "units":"mm"},
            	            {"classId":"sy", "deviceId":"sy", "title":"sy", "units":"mm"},
            	            {"classId":"sz", "deviceId":"sz", "title":"sz", "units":"mm"},
            	            {"classId":"dz", "deviceId":"dz", "title":"dz", "units":"mm"},
            	            {"classId":"stth", "deviceId":"stth", "title":"stth", "units":"deg"}
            	            ]
               }
               ];

var nsItems = [
               {"classId":"reactorPower", "deviceId":"reactorPower", "title":"Reactor Power", "units":"MW"}
               ];

var histmemUrl = null;