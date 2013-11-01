var title = "Wombat Status";
var devices = [
               {"group":"BEAM STATUS", 
            	   "items":[{"classId":"plc_tertiary", "deviceId":"plc_tertiary", "title":"Sample Shutter", "units":""},
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
               {"group":"SAMPLE STAGE", 
            	   "items":[{"classId":"stth", "deviceId":"stth", "title":"stth", "units":"deg"},  
            	            {"classId":"sx", "deviceId":"sx", "title":"sx", "units":"mm"},
            	            {"classId":"sy", "deviceId":"sy", "title":"sy", "units":"mm"},
            	            {"classId":"som", "deviceId":"som", "title":"som", "units":"deg"}
            	            ]
               },
               {"group":"SLITS", 
            	   "items":[{"classId":"ss1vg", "deviceId":"ss1vg", "title":"slit1 vert gap", "units":"mm"},  
            	            {"classId":"ss1hg", "deviceId":"ss1hg", "title":"slit1 horiz gap", "units":"mm"},
            	            {"classId":"ss2vg", "deviceId":"ss2vg", "title":"slit2 vert gap", "units":"mm"},
            	            {"classId":"ss2hg", "deviceId":"ss2hg", "title":"slit2 horiz gap", "units":"mm"}
            	   ]
               }
               ];

var histmemUrl = "dae/rest/image?screen_size_x=800&screen_size_y=600";