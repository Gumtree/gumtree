var title = "Bilby Status";
var batchEnabled = false;
var devices = [
               {"group":"NEUTRON BEAM", 
            	   "items":[{"classId":"monitor_counts", "deviceId":"monitor_counts", "title":"Monitor", "units":"cts"}, 
            	            {"classId":"detector_counts", "deviceId":"/instrument/detector/total_counts", "title":"Detector Counts", "units":"c"}, 
            	            {"classId":"total_detector_rate", "deviceId":"::histogram_memory::ratemap_xy_total", "title":"Tot. Rate on Detector", "units":"c/t"}, 
            	            {"classId":"xy_max_binrate", "deviceId":"::histogram_memory::ratemap_xy_max_bin", "title":"Max Rate on Pixel", "units":"c/t"}
            	            ]
               },
               {"group":"SLITS", 
            	   "items":[{"classId":"ss1hg", "deviceId":"ss1hg", "title":"ss1hg", "units":"mm"},
            	            {"classId":"ss1ho", "deviceId":"ss1ho", "title":"ss1ho", "units":"mm"},
            	            {"classId":"ss1vg", "deviceId":"ss1vg", "title":"ss1vg", "units":"mm"},
            	            {"classId":"ss1vo", "deviceId":"ss1vo", "title":"ss1vo", "units":"mm"}
            	            ]
               },
               {"group":"DETECTOR", 
            	   "items":[{"classId":"cdd", "deviceId":"cdd", "title":"cdd", "units":"mm"},
            	            {"classId":"cdl", "deviceId":"cdl", "title":"cdl", "units":"mm"},
            	            {"classId":"cdr", "deviceId":"cdr", "title":"cdr", "units":"mm"},
            	            {"classId":"cdu", "deviceId":"cdu", "title":"cdu", "units":"mm"},
            	            {"classId":"det", "deviceId":"det", "title":"det", "units":"mm"}
            	            ]
               },
               {"group":"ATTENUATOR", 
            	   "items":[{"classId":"att", "deviceId":"att", "title":"att", "units":"mm"}
            	            ]
               },
               {"group":"BEAM STOP", 
            	   "items":[{"classId":"bs", "deviceId":"/commands/beamstops/selbsxz/bs", "title":"bs", "units":""},
            	            {"classId":"bx", "deviceId":"/commands/beamstops/selbsxz/bx", "title":"bx", "units":"mm"},
            	            {"classId":"bz", "deviceId":"/commands/beamstops/selbsxz/bz", "title":"bz", "units":"mm"}
            	            ]
               }
               ];

var nsItems = [
               {"classId":"reactorPower", "deviceId":"reactorPower", "title":"Reactor Power", "units":"MW"},
               {"classId":"cnsInTemp", "deviceId":"cnsInTemp", "title":"CNS In Temp", "units":"K"},
               {"classId":"cnsOutTemp", "deviceId":"cnsOutTemp", "title":"CNS Out Temp", "units":"K"}
               ];

var histmemUrl = "dae/rest/image?type=TOTAL_HISTOGRAM_XY&screen_size_x=800&screen_size_y=600";