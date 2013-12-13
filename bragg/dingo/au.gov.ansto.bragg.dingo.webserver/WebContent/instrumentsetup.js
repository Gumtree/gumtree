var title = "Dingo Status";
var devices = [
               {"group":"CAMERA STATUS", 
            	   "items":[{"classId":"plc_secondary", "deviceId":"plc_secondary", "title":"Secondary Shutter", "units":""},
            	            {"classId":"plc_tertiary", "deviceId":"plc_tertiary", "title":"Sample Shutter", "units":""},
            	            {"classId":"cm1_mode", "deviceId":"cm1_mode", "title":"CM1 Mode", "units":""},  
            	            {"classId":"cm1_preset", "deviceId":"cm1_preset", "title":"CM1 Preset", "units":""},
            	            {"classId":"cm1_time", "deviceId":"cm1_time", "title":"CM1 Time", "units":"s"},
            	            {"classId":"cm1_counts", "deviceId":"cm1_counts", "title":"CM1 Counts", "units":"c"}
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