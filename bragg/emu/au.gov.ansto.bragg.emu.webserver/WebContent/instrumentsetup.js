var title = "Emu Status";
var batchEnabled = true;
var devices = [
               {"group":"BEAM STATUS", 
            	   "items":[{"classId":"total_counts", "deviceId":"::histogram_memory::total_counts", "title":"Detector Counts", "units":"ct"}, 
            	            {"classId":"histogram_memory_time", "deviceId":"::histogram_memory::time", "title":"Time of Counting", "units":"s"}, 
            	            {"classId":"bm1_counts", "deviceId":"bm1_counts", "title":"BM1 Counts", "units":"ct"},
            	            {"classId":"bm2_counts", "deviceId":"bm2_counts", "title":"BM2 Counts", "units":"ct"},
            	            {"classId":"ratemap_xy_total", "deviceId":"::histogram_memory::ratemap_xy_total", "title":"Detector Rate", "units":"ct"}
            	            ]
               },
               {"group":"APERTURE", 
            	   "items":[{"classId":"sv1", "deviceId":"sv1", "title":"sv1", "units":"mm"},  
            	            {"classId":"sh1", "deviceId":"sh1", "title":"sh1", "units":"mm"},
            	            {"classId":"sv2", "deviceId":"sv2", "title":"sv2", "units":"mm"},
            	            {"classId":"sh2", "deviceId":"sh2", "title":"sh2", "units":"mm"}
            	            ]
               },
               {"group":"MONOCHROMATOR", 
            	   "items":[{"classId":"vwi", "deviceId":"vwi", "title":"wavelength", "units":"Å"},  
            	            {"classId":"mom", "deviceId":"mom", "title":"mom", "units":"deg"},
            	            {"classId":"mtth", "deviceId":"mtth", "title":"mtth", "units":"deg"},
            	            {"classId":"moma", "deviceId":"moma", "title":"moma", "units":"deg"},
            	            {"classId":"momb", "deviceId":"momb", "title":"momb", "units":"deg"},
            	            {"classId":"momc", "deviceId":"momc", "title":"momc", "units":"deg"},
            	            {"classId":"mra", "deviceId":"mra", "title":"mra", "units":"deg"},
            	            {"classId":"mrb", "deviceId":"mrb", "title":"mrb", "units":"deg"},
            	            {"classId":"mrc", "deviceId":"mrc", "title":"mrc", "units":"deg"}
            	            ]
               },
               {"group":"FERMI CHOPPER", 
            	   "items":[{"classId":"mchs", "deviceId":"mchs", "title":"master chopper", "units":"rpm"},  
            	            {"classId":"schs", "deviceId":"schs", "title":"slave chopper", "units":"rpm"}
            	   ]
               }
               ];

var nsItems = [
               {"classId":"reactorPower", "deviceId":"reactorPower", "title":"Reactor Power", "units":"MW"},
               {"classId":"cnsInTemp", "deviceId":"cnsInTemp", "title":"CNS In Temp", "units":"K"},
               {"classId":"cnsOutTemp", "deviceId":"cnsOutTemp", "title":"CNS Out Temp", "units":"K"}
               ];

var histmemUrl = "dae/rest/image?type=TOTAL_HISTOGRAM_XT&screen_size_x=760&screen_size_y=760";