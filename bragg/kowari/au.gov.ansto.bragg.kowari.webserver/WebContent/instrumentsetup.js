var title = "Kowari Status";
var batchEnabled = true;
var devices = [
               {"group":"BEAM STATUS", 
            	   "items":[{"classId":"plc_tertiary", "deviceId":"plc_tertiary", "title":"Sample Shutter", "units":""},
            	            {"classId":"total_detector_rate", "deviceId":"::histogram_memory::ratemap_xy_total", "title":"Tot. Rate on Detector", "units":"c/s"},  
            	            {"classId":"histogram_memory_time", "deviceId":"::histogram_memory::time", "title":"Time of Counting", "units":"s"}, 
            	            {"classId":"xy_max_binrate", "deviceId":"::histogram_memory::ratemap_xy_max_bin", "title":"Max Rate on Pixel", "units":"c/s"},
            	            {"classId":"bm1_event_rate", "deviceId":"bm1_event_rate", "title":"Monitor Rate", "units":"c/s"}
            	            ]
               },
               {"group":"PRIMARY SLITS", 
            	   "items":[{"classId":"psw", "deviceId":"psw", "title":"PS Width", "units":"mm"},
            	            {"classId":"psho", "deviceId":"psho", "title":"PS Offset", "units":"mm"},
            	            {"classId":"psp", "deviceId":"psp", "title":"PS Position", "units":"mm"}
            	            ]
               },
               {"group":"SECONDARY SLITS", 
            	   "items":[{"classId":"ssw", "deviceId":"psw", "title":"PS Width", "units":"mm"},
            	            {"classId":"ssho", "deviceId":"psho", "title":"PS Offset", "units":"mm"},
            	            {"classId":"ssp", "deviceId":"psp", "title":"PS Position", "units":"mm"}
            	            ]
               },
               {"group":"SAMPLE STAGE", 
            	   "items":[  
            	            {"classId":"sx", "deviceId":"sx", "title":"x translation", "units":"mm"},
            	            {"classId":"sy", "deviceId":"sy", "title":"y translation", "units":"mm"},
            	            {"classId":"sz", "deviceId":"sz", "title":"z translation", "units":"mm"},
            	            {"classId":"som", "deviceId":"som", "title":"omega", "units":"deg"}
            	            ]
               },
               {"group":"TWO THETA", 
            	   "items":[{"classId":"mtth", "deviceId":"mtth", "title":"mtth", "units":"deg"},  
            	            {"classId":"stth", "deviceId":"stth", "title":"stth", "units":"deg"}
            	   ]
               }
               ];

var nsItems = [
               {"classId":"reactorPower", "deviceId":"reactorPower", "title":"Reactor Power", "units":"MW"}
               ];

var histmemUrl = "dae/rest/image?type=TOTAL_HISTOGRAM_XY&screen_size_x=800&screen_size_y=600";