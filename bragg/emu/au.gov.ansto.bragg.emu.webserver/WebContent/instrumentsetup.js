var title = "Emu";
var batchEnabled = true;
var devices = [
               {"group":"NEUTRON COUNTS", 
            	   "items":[{"classId":"total_counts", "deviceId":"::histogram_memory::total_counts", "title":"Detector Counts", "units":"ct"}, 
            	            {"classId":"histogram_memory_time", "deviceId":"::histogram_memory::time", "title":"Time of Counting", "units":"s"}, 
            	            {"classId":"bm1_counts", "deviceId":"bm1_counts", "title":"BM1 Counts", "units":"ct"},
            	            {"classId":"bm2_counts", "deviceId":"bm2_counts", "title":"BM2 Counts", "units":"ct"},
            	            {"classId":"ratemap_xy_total", "deviceId":"::histogram_memory::ratemap_xy_total", "title":"Detector Rate", "units":"ct"}
            	            ]
               },
               {"group":"PREMONOCHROMATOR", 
            	   "items":[{"classId":"lambda", "deviceId":"lambda", "title":"Wavelength", "units":"\u212B"},  
            	            {"classId":"momto", "deviceId":"momto", "title":"Take-off angle", "units":"degree"},
            	            {"classId":"mom", "deviceId":"mom", "title":"Premono Omega", "units":"degree"}
            	            ]
               },
               {"group":"CHOPPERS", 
            	   "items":[{"classId":"chom", "deviceId":"chom", "title":"Graphite chopper omega", "units":"degree"},  
            	            {"classId":"chomto", "deviceId":"chomto", "title":"Graphite chopper take-off angle", "units":"degree"},
            	            {"classId":"gspeed", "deviceId":"/instrument/chpr/graphite/actspeed", "title":"Graphite chopper speed", "units":"rpm"},
            	            {"classId":"bspeed", "deviceId":"/instrument/chpr/background/actspeed", "title":"Background chopper speed", "units":"rpm"},
            	            {"classId":"bphase", "deviceId":"/instrument/chpr/background/actphase", "title":"Background chopper phase", "units":"degree"},
            	            {"classId":"bgear", "deviceId":"/instrument/chpr/background/actgear", "title":"Background chopper ratio", "units":""}
            	            ]
               },
               {"group":"DOPPLER", 
            	   "items":[{"classId":"damp", "deviceId":"/instrument/doppler/ctrl/amplitude", "title":"Amplitude", "units":"mm"},
            	            {"classId":"dvel", "deviceId":"/instrument/doppler/ctrl/velocity", "title":"Velocity", "units":"m/s"}
            	   ]
               }
               ];

var nsItems = [
               {"classId":"reactorPower", "deviceId":"reactorPower", "title":"Reactor Power", "units":"MW"},
               {"classId":"cnsInTemp", "deviceId":"cnsInTemp", "title":"CNS In Temp", "units":"K"},
               {"classId":"cnsOutTemp", "deviceId":"cnsOutTemp", "title":"CNS Out Temp", "units":"K"}
               ];

var histmemUrl = "dae/rest/image?type=TOTAL_HISTOGRAM_XT&screen_size_x=760&screen_size_y=760";