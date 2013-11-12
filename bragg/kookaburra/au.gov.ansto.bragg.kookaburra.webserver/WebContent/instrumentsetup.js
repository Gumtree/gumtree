var title = "Kookaburra Status";
var devices = [
               {"group":"NEUTRON BEAM", 
            	   "items":[{"classId":"monitor_counts", "deviceId":"monitor_counts", "title":"Monitor", "units":"cts"}, 
            	            {"classId":"detector_counts", "deviceId":"/instrument/detector/total_counts", "title":"Detector Counts", "units":"c"}, 
            	            {"classId":"total_detector_rate", "deviceId":"::histogram_memory::ratemap_xy_total", "title":"Tot. Rate on Detector", "units":"c/t"}, 
            	            {"classId":"xy_max_binrate", "deviceId":"::histogram_memory::ratemap_xy_max_bin", "title":"Max Rate on Pixel", "units":"c/t"}
            	            ]
               },
               {"group":"PREMONOCHROMATOR", 
            	   "items":[{"classId":"pmom", "deviceId":"pmom", "title":"pmom", "units":"deg"},
            	            {"classId":"pmchi", "deviceId":"pmchi", "title":"pmchi", "units":"deg"}
            	            ]
               },
               {"group":"BE FILTER", 
            	   "items":[{"classId":"bex", "deviceId":"bex", "title":"bex", "units":"mm"}
            	            ]
               },
               {"group":"CHANNEL-CUT MONOCHROMATOR", 
            	   "items":[{"classId":"m1om", "deviceId":"m1om", "title":"m1om", "units":"deg"},
            	            {"classId":"m1chi", "deviceId":"m1chi", "title":"m1chi", "units":"deg"},
            	            {"classId":"m1x", "deviceId":"m1x", "title":"m1x", "units":"mm"}
            	            ]
               },
               {"group":"SLIT 1", 
            	   "items":[{"classId":"ss1u", "deviceId":"ss1u", "title":"ss1u", "units":"mm"},
            	            {"classId":"ss1d", "deviceId":"ss1d", "title":"ss1d", "units":"mm"},
            	            {"classId":"ss1r", "deviceId":"ss1r", "title":"ss1r", "units":"mm"},
            	            {"classId":"ss1l", "deviceId":"ss1l", "title":"ss1l", "units":"mm"}
            	            ]
               },
               {"group":"CHANNEL-CUT ANALYSER", 
            	   "items":[{"classId":"m2om", "deviceId":"m2om", "title":"m2om", "units":"deg"},
            	            {"classId":"m2chi", "deviceId":"m2chi", "title":"m2chi", "units":"deg"},
            	            {"classId":"m2x", "deviceId":"m2x", "title":"m2x", "units":"mm"},
            	            {"classId":"m2y", "deviceId":"m2y", "title":"m2y", "units":"mm"}
            	            ]
               },
               {"group":"ATTENUATOR", 
            	   "items":[{"classId":"att", "deviceId":"att", "title":"att", "units":"mm"}
            	            ]
               },
               {"group":"SLIT 2", 
            	   "items":[{"classId":"ss2u", "deviceId":"ss2u", "title":"ss2u", "units":"mm"},
            	            {"classId":"ss2d", "deviceId":"ss2d", "title":"ss2d", "units":"mm"},
            	            {"classId":"ss2r", "deviceId":"ss2r", "title":"ss2r", "units":"mm"},
            	            {"classId":"ss2l", "deviceId":"ss2l", "title":"ss2l", "units":"mm"}
            	            ]
               },
               {"group":"MAIN DETECTOR", 
            	   "items":[{"classId":"mdet", "deviceId":"mdet", "title":"mdet", "units":"mm"}
            	            ]
               }
               ];

var histmemUrl = "dae/rest/image?type=TOTAL_HISTOGRAM_XY&screen_size_x=600&screen_size_y=600";